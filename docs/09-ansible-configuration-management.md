# 9. Configuration management and provisioning with Ansible (Tasks 14, 15)

## 9.1 Configuration specification (Task 14)

What a target node needs before the Vehicle Service Booking Portal can run:

| Category | Requirement | Value in this project |
|----------|-------------|-----------------------|
| **OS** | Ubuntu 22.04 / 24.04 (Debian family, systemd) | `ubuntu/jammy64` in the Vagrantfile |
| **Packages** | Java 17 runtime | `openjdk-17-jre-headless` |
| | HTTP client for health checks | `curl` |
| **User / group** | Unprivileged service account, no login shell | `vsbp:vsbp` (system user, `/usr/sbin/nologin`) |
| **Folders** | Application base | `/opt/vsbp` (root:vsbp 0755) |
| | Versioned releases | `/opt/vsbp/releases/<version>/` |
| | Configuration | `/etc/vsbp` (root:vsbp 0750) |
| | Database files (H2) | `/var/lib/vsbp` (vsbp:vsbp 0750) |
| | Logs | `/var/log/vsbp` (vsbp:vsbp 0750) |
| **Files** | Release artefact | `/opt/vsbp/releases/<version>/vehicle-service-portal.war` |
| | Active release link | `/opt/vsbp/current -> releases/<version>` |
| | Previous release marker (for rollback) | `/opt/vsbp/previous_version` |
| | Environment file | `/etc/vsbp/vsbp.env` (0640) with `APP_ENVIRONMENT`, `SERVER_PORT`, `DB_URL`, `JAVA_OPTS` |
| | systemd unit | `/etc/systemd/system/vsbp.service` |
| **Ports** | Application HTTP | TCP **8080** (`SERVER_PORT`); the embedded H2 database opens no network port |
| **Services** | Application | `vsbp.service` - enabled at boot, `Restart=on-failure` |
| **Health** | Readiness | `GET http://127.0.0.1:8080/actuator/health` -> `{"status":"UP"}` |

### What the automation contains
```
ansible/
|-- ansible.cfg            defaults (inventory, no retry files)
|-- inventory.ini          group [vsbp_servers]; local connection or SSH
|-- site.yml               play: provision + deploy (role vsbp)
|-- rollback.yml           play: restore the previous release
|-- Vagrantfile            clean Ubuntu VM that runs site.yml with ansible_local
`-- roles/vsbp/
    |-- defaults/main.yml  every setting in one place (version, environment, ports, folders)
    |-- tasks/main.yml     packages -> user -> folders -> release -> config -> service -> health
    |-- handlers/main.yml  reload systemd, restart vsbp
    `-- templates/         vsbp.env.j2, vsbp.service.j2
```

Design points: tasks use state-based modules (`apt`, `user`, `file`, `copy`, `template`, `systemd`), so a second run changes nothing
(idempotency). Releases are kept side by side and `current` is a symlink, so rollback is a symlink switch and a restart.
Tags (`packages`, `user`, `dirs`, `release`, `config`, `service`, `health`) allow partial runs.

## 9.2 Run it (first execution log)

You need a **clean Ubuntu node** and a built WAR (`mvn clean package -DskipTests` on your machine).

| Option | When | Commands |
|--------|------|----------|
| **Vagrant + VirtualBox** (recommended on Windows) | Clean machine on demand, Ansible runs inside the VM | `cd ansible` -> `vagrant up` |
| **WSL2 Ubuntu with systemd** | No VM software | enable systemd in `/etc/wsl.conf` (`[boot]` / `systemd=true`), `wsl --shutdown`, install Ansible (`sudo apt install ansible`), then `cd ansible && ansible-playbook site.yml` |
| **Ubuntu server / cloud VM over SSH** | A real remote node | edit `inventory.ini` (Option B), `ansible-playbook site.yml` |

```bash
cd ansible
ansible-playbook site.yml --syntax-check
ansible-playbook site.yml                       # first execution: tasks show "changed"
```
Open <http://localhost:8085/> (Vagrant port forward) or `http://<node-ip>:8080/`.

Save the console output as the "first execution log" (`ansible-playbook site.yml | tee evidence/ansible-first-run.log`).

## 9.3 Reliability validation (Task 15)

### Idempotency
```bash
ansible-playbook site.yml                       # run it again
```
Expected recap: `changed=0` (the `apt` cache refresh only counts once per hour). Capture the first-run and second-run `PLAY RECAP` lines side by side.

### Health check
The last task of the role waits (up to 2 minutes) for `/actuator/health` to return `UP`. Manual check:
```bash
curl -s http://127.0.0.1:8080/actuator/health          # on the node
systemctl status vsbp --no-pager                       # service is active (running)
```

### Deploy a new release, then roll back
```bash
# build a changed WAR (e.g. edit the home page heading) then:
ansible-playbook site.yml -e vsbp_version=1.1.0        # new release folder, symlink switched, service restarted
ls -l /opt/vsbp /opt/vsbp/releases                     # both releases present, current -> 1.1.0
ansible-playbook rollback.yml                          # current -> 1.0.0, restart, health check passes
ls -l /opt/vsbp | grep current                         # current -> /opt/vsbp/releases/1.0.0
```
`rollback.yml` restores the version recorded in `/opt/vsbp/previous_version` (or `-e rollback_to=<version>`), records the release it
leaves so it can be restored again, restarts the service and fails if the health check does not report `UP`.

**Recovery test (service crash):** `sudo systemctl kill -s KILL vsbp` - systemd restarts it after 5 seconds
(`Restart=on-failure`); `curl` the health URL until it answers `UP` again.

### Clean-environment proof
`vagrant destroy -f && vagrant up` rebuilds the node from nothing with one command; the health check passes at the end of provisioning.

## 9.4 What was validated while writing this chapter

The playbooks were checked with `ansible-playbook --syntax-check` and `ansible-lint` (profile *production*, 0 failures) with
ansible-core 2.21. In a throw-away Linux sandbox **without systemd, apt access or Java**, the user, folder, release, symlink,
template and rollback tasks were executed with `--skip-tags packages,service,health`:

| Run | Command | Result |
|-----|---------|--------|
| 1. clean node, release 1.0.0 | `site.yml -e vsbp_version=1.0.0` | `ok=14 changed=8 failed=0` |
| 2. same inputs again | same command | `ok=13 changed=0 failed=0` (**idempotent**) |
| 3. upgrade to 1.1.0 | `site.yml -e vsbp_version=1.1.0` | `changed=4`; `current -> releases/1.1.0`, `previous_version = 1.0.0` |
| 4. rollback | `rollback.yml` | `current -> releases/1.0.0`, `previous_version = 1.1.0` |

The `apt`, `systemd` and health-check tasks need a real Ubuntu node and were **not** executed in that sandbox. Run the playbook once on your
node and keep that console output as your own evidence.

## 9.5 Evidence checklist
Configuration table above; `ansible/` folder; first-run log (`changed` tasks); second-run recap `changed=0`; `curl` health result and
`systemctl status vsbp`; `ls -l /opt/vsbp` before and after rollback; browser screenshot of the application on the provisioned node.
