# how to
create file:
```
vim /etc/systemd/system/origin.service
```
type there:
```
[Unit]
Description=origin server
After=network.target
After=nginx.service
After=mariadb.service
Requires=mariadb.service

[Service]
Type=forking
ExecStart=/usr/local/origin/backend/run.sh
ExecStop=/usr/local/origin/backend/stop.sh
ExecReload=/usr/local/origin/backend/restart.sh

PIDFile=/usr/local/origin/backend/process.pid
WorkingDirectory=/usr/local/origin/backend
OOMScoreAdjust=-1000
TimeoutSec=30

Restart=always
RestartSec=30
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
```

run
```shell
systemctl daemon-reload
systemctl enable origin.service
systemctl start origin.service
```