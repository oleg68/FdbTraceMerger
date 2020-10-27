# FdbHash
A tool for calculating checksums and comparing two foundationdb databases

# Building
1. Clone the source repository
```
    git clone https://github.com/oleg68/FdbTraceMerger.git
```
2. Go to the local repository directory
```
    cd FdbTraceMerger
```
3. Execute
```
    mvn install
```
  The resulting jar will be installed to the .m2/repository/com/openwaygroup/dbkernel/fdb/fdb-trace-merger subdirectory of your home directory.
  The required fdb client library will be copied to the target/lib subdirectory

# Deploying
1. Make a directory for deploying. It is named as ``Delploy Directory``.
2. Copy fdb-trace-merger-03.52.29.10.jar from .m2/repository/com/openwaygroup/dbkernel/fdb/fdb-hash/03.52.29.10/ subdirectory of your home directory to ``Delploy Directory``.

# Usage
   run java -jar ``Delploy Directory``/fdb-trace-merger-03.52.29.10.jar -help for details
