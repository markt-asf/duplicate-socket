# duplicate-socket
Test case to demonstrate duplicate sockets being returned from ServerSocketChannel.accept()

To recreate the issue:
- run the NetworkTest java class in this repository
- from separate console windows, run at least two instance of the following

```
for i in {1..50}; do /opt/wrk/wrk -t 2 -c 1000 -d 5s --latency --timeout 1s --script ./myPost.lua http://localhost:8080/post; done
```

There does appear to be a timing element to this test and running more instances of the above wrk loop in additional console windows may increase the likelihood od triggering the error.