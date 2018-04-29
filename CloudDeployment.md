
**Non Prod Bindings service**

cf cups sequential-db -p '{   
    "alias" : "sequential-db",
    "url"   : "jdbc:postgresql://npAwsDbPath:portNumber/dbInstanceName",
    "user"  : "*******",
    "password"  : "******"
}'

**Prod Bindings service**

cf cups sequential-db -p '{   
    "alias" : "sequential-db",
    "url"   : "jdbc:postgresql://prodAwsDbPath:portNumber/dbInstanceName",
    "user"  : "******",
    "password"  : "******"
}'