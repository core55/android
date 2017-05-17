# Contribute

## Folder structure

* `Activity` : all the activities of the app
* `Entity` : entities that reflex what the API returns
* `Helper` : helper classes
* `Model` : models that are used in the Android app
* `Service` : all services that run in background

```
│
└───Activity
│   │   ApplinkActivity
│   │   CreateActivity
│   │   EmailNotConfirmedActivity
│   │   LoginActivity
│   │   MapActivity
│   │   RegisterActivity
│   │   WelcomeActivity
│
└───Entity
│   │   BaseEntity
│   │   Meetup
│   │   User    
│  
└───Helper
│   │   AuthenticationHelper
│   |   CircleTransform
│   |   DrawerFragment.kt
│   |   GsonRequest
│   |   HeaderRequest
│   |   HttpRequestHelper
│   |   LocationHelper
│   |   NavigationDrawer
│   |   OutOfBoundsHelper
│   |   UserAdapter
│   |   VolleyController
│
└───Model
│   │   AccountCredentials
│   |   AuthenticationResponse
│   |   DataHolder
│   |   GoogleToken
│   |   StringResponse
│   |   UserList
│ 
└───Service
    │   LocationManager
    |   LocationService
    |   NetworkService
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
