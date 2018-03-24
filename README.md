[ ![Download](https://api.bintray.com/packages/efff/maven/RotorKotlinDatabase/images/download.svg) ](https://bintray.com/efff/maven/RotorKotlinDatabase/_latestVersion)
<p align="center"><img width="10%" vspace="20" src="https://github.com/rotorlab/database-kotlin/raw/develop/app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png"></p>

# Rotor Database
Rotor Database is a complementary module for Rotor Core (kotlin). It allows to work with shared (Java) objects between many devices offering users real time changes and better mobile data consumption. 

Forget things like swipe-to-refresh events, lots of server requests and object storage management. Why? 

**Rotor Database philosophy** states that the only needed requests are those that change data on remote database. That means that the rest of requests you are imaging (give me updates, give updates, give me updates) are replaced now. How?

Rotor Core is connected to Rotor and Redis servers. The first one controls object sharing queues, devices waiting for changes and all data edition on remote database. The second (as you probably know) gives us Pub/Sub messaging pattern for data changes replication.

Before use this lib, check out [Rotor Core repo](https://github.com/rotorlab/core-kotlin) for its initialization.

## Implementation
Import libraries:

```groovy
android {
    defaultConfig {
        multiDexEnabled true
    }
}
 
def rotor_version =  "0.1.1"
 
dependencies {
    implementation ("com.rotor:core:$rotor_version@aar") {
        transitive = true
    }
    implementation ("com.rotor:database:$rotor_version@aar") {
        transitive = true
    }
}
```
Initialize database module after Rotor core initialization:
```java
Rotor.initialize(getApplicationContext(), "http://10.0.2.2:1507/", "redis://10.0.2.2", new StatusListener() {
    @Override
    public void connected() {
         Database.initialize()
    }
    
    @Override
    public void reconnecting() {
         
    }
});
```
## Listen shared object changes
Rotor Database allows devices to work with the same objects by listening the same `path`. When an object is listened, library says to Rotor server your device is waiting for changes on that `path`, so every time any device makes a change on that (object), the differences are calculated and replicated on all devices listening.
For that we have `Database.listen(...)` method which has a simple **object lifecycle interface** for control every object state.

```kotlin
// kotlin
Reference<T>(T::class.java) {
 
    fun onCreate()
 
    fun onUpdate(): T ?
 
    fun onChanged(ref: T)
    
    fun onDestroy()
 
    fun progress(value: Int)
    
}
```

### onCreate
Called when object is not created in remote DB yet. Object is defined and synchronized with server here. This method won't be called if object already exists on server, `onChange` method will be called instead.
```java
@Override
public void onCreate() {
    objectA = new ObjectA("foo");
    Database.sync(path);
}
```
### onChanged
Called in two situations, when some device has made changes on the same object and when `listen` method is called and the object is cached. Database library pass the object up to date as parameter.
```java
@Override
public void onChanged(ObjectA objectA) {
    this.objectA = objectA;  
    // notify change on UI
}
```
### onUpdate
Called when `sync` method is invoked. Differences with the last "fresh" object passed by library are calculated and sent to server.
```java
@Override
public ObjectA onUpdate() {
    return objectA;
}
```
### onDestroy
Called when `remove` method is invoked. Reference is removed on database too.
```java
@Override
public void onDestroy() {
    objectA = null;
    // UI changes for removed object
}
```
### progress
Some object updates can become too big, so server slices updates and sends them sequentially. value parameter goes from 0 to 100
```java
@Override
public void progress(int value) {
    Log.e(TAG, "loading " + path + " : " + value + " %");
}
```
After work with objects, changes must be synchronized with servers. Call `Database.sync(path: String)` method to sync it.
```java
Database.sync("myObjects/objectA");
```
Remove listener in server by calling `Database.unlisten(path: String)`.
```java
Database.unlisten("myObjects/objectA");
```
Remove reference in database by calling `Database.remove(path: String)`. `onDestroy` will be called.
```java
Database.remove("myObjects/objectA");
```
Samples:
```java
// java
 
class ObjectA {
    String value;
    public ObjectA(String value) {
        this.value = value;
    }
    public void setValue(String value) {
        this.value = vaue;
    }
    public void getValue() {
        return value;
    }
}
 
String path = "myObjects/objectA";
ObjectA objectA = null;
  
Database.listen(path, new Reference<ObjectA>(ObjectA.class) {
    @Override
    public void onCreate() {
        objectA = new ObjectA("foo");
        Database.sync(path);
    }
    
    @Override
    public void onChanged(ObjectA objectA) {
        this.objectA = objectA;  
        // notify change on UI
    }
    
    @Override
    public ObjectA onUpdate() {
        return objectA;
    }
    
    @Override
    public void onDestroy() {
        objectA = null;
        // UI changes for removed object
    }
 
    @Override
    public void progress(int value) {
        Log.e(TAG, "loading " + path + " : " + value + " %");
    }
});
```

```kotlin
// kotlin
 
data class ObjectA(var value: String)
var path = "myObjects/objectA"
var objectA: ObjectA ? = null
Database.listen(path, object: Reference<ObjectA>(ObjectA::class.java) {
    override fun onCreate() {
        objectA = ObjectA("foo")
        Database.sync(path);
    }
 
    override fun onUpdate(): ObjectA ? {
        return this@MainActivity.objectA
    }
 
    override fun onChanged(ref: ObjectA) {
        this@MainActivity.objectA = ref
        // notify change on UI
    }
    
    override fun onDestroy() {
        this@MainActivity.objectA = null;
        // UI changes for removed object
    }
 
    override fun progress(value: Int) {
        Log.e("rotor", "loading " + path + " -> " + value + " %")
    }
})
```

License
-------
    Copyright 2018 RotorLab Organization

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
