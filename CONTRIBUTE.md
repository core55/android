# Contribute

## Folder structure

* `Activity` : all the activities/views of the app
* `Entity` : entities that reflex what the API returns
* `Helper` : helper classes for different functions
* `Model` : models to store information that are used in the Android app
* `Service` : all services that run in background

```
│
└───Activity
│   │   ApplinkActivity: entry point when clicking app link
│   │   CreateActivity: view to create a meetup
│   │   EmailNotConfirmedActivity: view when email is not confirmed
│   │   LoginActivity: login view
│   │   MapActivity: main activity where the map is displayed with the drawer
│   │   RegisterActivity: register view
│   │   WelcomeActivity: entry point when first starting the app
│
└───Entity
│   │   BaseEntity: common fields for Meetup and User
│   │   Meetup: meetup entity reflex from API
│   │   User: user entity reflex from API
│  
└───Helper
│   │   AuthenticationHelper
│   |   CircleTransform
│   |   DrawerFragment.kt: drawer implement in Kotlin
│   |   GsonRequest: custom request which reflex object from the API
│   |   HeaderRequest: request with custom HTTP header
│   |   HttpRequestHelper: offers method for HTTP handling
│   |   LocationHelper: offers method for location handling
│   |   NavigationDrawer (not used)
│   |   OutOfBoundsHelper
│   |   UserAdapter (not used)
│   |   VolleyController (not used)
│
└───Model
│   │   AccountCredentials
│   |   AuthenticationResponse
│   |   DataHolder: contains the current Meetup and User as a singleton
│   |   GoogleToken
│   |   StringResponse
│   |   UserList
│ 
└───Service
    │   LocationManager: manage LocationService 
    |   LocationService: request location updates in background
    |   NetworkService: periodic request with the API in background
```

## Guidelines

* Append your name to the end of the authors header in every modified file like this:
```java
/*
  Authors: 
  Marcel Eschmann
  Hussam Hassanein
  Patrick Richer St-Onge
  Juan Luiz Ruiz-Tagle Oriol
  Simone Stefani
  < new name >
 */
```
* `CTRL + ALT + L` or `CMD + ALT (option) + L` before committing to auto-format the file
* Write Javadoc on top of classes and methods
* Place strings in `res/values/strings.xml`
