# beacon

Tiny Java app that meshes a UniFi UDM uplink IP with AWS Route 53 DNS.

This app consumes the UniFi controller API to discover the public facing WAN-uplink IP of my UDM. Then, it updates a DNS record at AWS Route 53 with the corresponding IP. This ensures that my WAN uplink and DNS records stay in sync, so that anytime my ISP issued IP address changes the corresponding DNS record(s) are automatically updated at AWS Route 53.

## Running

Run the app by invoking:

    java -Dconfig.file=/path/to/your/beacon.conf -jar dist/beacon-0.1-runnable.jar

Once running, hit the tiny web-server at http://localhost:8080/beacon and enjoy!

### Configuration

Beacon is configured with HOCON using [lightbend/config](https://github.com/lightbend/config).

In development, you should create a `~/beacon-dev.conf` file with the following contents:

```hocon
include "application"

beacon {
  context-path = "/beacon"
  base-uri = "http://localhost:8080"

  dev-mode = true

  udm {
    api-base-url = "https://[YOUR CONTROLLER HOSTNAME OR IP HERE]"
    api-client-timeout = 10s

    username = "[YOUR CONTROLLER USERNAME HERE]"
    password = "[YOUR CONTROLLER PASSWORD HERE]"
  }

  aws {
    access-key = "[YOUR AWS ACCESS KEY HERE]"
    secret-key = "[YOUR AWS SECRET KEY HERE]"
    
    route-53 {
      region = "us-east-1"
      hosted-zone-id = "[ROUTE 53 HOSTED ZONE ID TO UPDATE HERE]"
      resource-record-upsert-name = "[RECORD TO UDPATE HERE]"
      resource-record-upsert-ttl = 30m
    }
  }

  quartz {
    thread-pool {
      size = 1
      use-daemons = true
    }

    // Run every 15-minutes
    cron-expression = "0 0/15 * 1/1 * ? *"
  }
}

```

When running Beacon locally, specify your configuration file by passing a `-Dconfig.file` system property on the command line:

```
java -Dconfig.file=/path/to/your/beacon.conf -jar dist/beacon-0.1-runnable.jar
```

## Licensing

Copyright (c) 2024 <a href="https://mark.koli.ch">Mark S. Kolich</a>.

All code in this project is freely available for use and redistribution under the <a href="http://opensource.org/comment/991">MIT License</a>.

See <a href="https://github.com/markkolich/beacon/blob/master/LICENSE">LICENSE</a> for details.
