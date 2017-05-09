## Contribute

* clone repository
* copy `res/values/google_maps_api.example.xml` to `res/values/google_maps_api.xml` and add your own Google Maps for Android API key
* copy `res/values/google_signin_api.example.xml` to `res/values/google_signin_api.xml` and request the key from the android team, 
we're using a shared one right now
* request the `google-services.json` from the team and copy to `app/google-services.json`


### Guidelines

* Always header with authors like this:
```java
/*
  Authors: S. Stefani
 */
```
* `CTRL + ALT + L` or `CMD + ALT (option) + L` before committing to auto-format the file
* Write nice Javadoc comments on top of classes and methods. Intellij automatically generates if you start typing `/**`
* Try to keep files short
* Be DRY -> dont repeat yourself
* Don't hardcode strings unless necessary (try not do it anyway)
* Learn what is the `Context` and the current activity
