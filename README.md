# JoinUp Android App

The official JoinUp app for Android. JoinUp allows people to spontaneously meet up anywhere and see the location of their friends on a map.

## Beta

[Google Play](https://play.google.com/store/apps/details?id=io.github.core55.joinup)

[Beta Community](https://plus.google.com/communities/102146297063147505085)

## Contribute

* Clone repository
* Copy `res/values/google_maps_api.example.xml` to `res/values/google_maps_api.xml` and add your own Google Maps for Android API key
* Copy `res/values/google_signin_api.example.xml` to `res/values/google_signin_api.xml` and request the key from the android team, we're using a shared one right now
* Request the `google-services.json` from the team and copy to `app/google-services.json`

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

## Authors

Made by [Core 55](https://core55.github.io/)

## License

TBA
