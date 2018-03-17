<p align="center"><img width="10%" vspace="20" src="https://github.com/rotorlab/database-kotlin/raw/develop/app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png"></p>

# Rotor Database

Database module for Rotor

### Usage
Import library:

```groovy
android {
    defaultConfig {
        multiDexEnabled true
    }
}
 
dependencies {
    implementation "com.rotor:core:$rotor_version"
    implementation "com.rotor:database:$rotor_version"
    
    // database dependencies
    implementation 'com.efraespada:jsondiff:1.1.0'
    implementation "com.squareup.retrofit2:retrofit:2.3.0"
    implementation "com.squareup.retrofit2:adapter-rxjava2:2.3.0"
    implementation "com.squareup.retrofit2:converter-gson:2.3.0"
    implementation "io.reactivex.rxjava2:rxandroid:2.0.2"
    implementation 'com.google.code.findbugs:jsr305:2.0.1'
    implementation 'com.google.guava:guava:22.0-android'
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"
    implementation "com.stringcare:library:0.7"
}
```
Initialize database module after Rotor initialization:
```java
Rotor.initialize(...)
Database.initialize()
```
Listen object changes:
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
 
ObjectA objectA = null;
  
Database.listener(path, new Reference<ObjectA>(ObjectA.class) {
    
    /**
    * called when listener is created on server, there is nothing stored
    * on db on the given path and onUpdate() still returning null
    */
    @Override
    public void onCreate() {
        objectA = new ObjectA("foo");
        
        // sync with server
        Database.sync(path);
    }
    
    /**
    * called after reference is synchronized with server
    * or is ready to be used.
    */
    @Override
    public void onChanged(ObjectA objectA) {
        this.objectA = objectA;  
        // notify change on UI
    }
    
    /**
    * gets new differences from local object
    */
    @Override
    public ObjectA onUpdate() {
        return objectA;
    }
 
    /**
    * long server updates, from 0 to 100
    */
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
Database.listener(path, object: Reference<ObjectA>(ObjectA::class.java) {
    override fun onCreate() {
        objectA = ObjectA("foo")
        
        // sync with server
        Database.sync(path);
    }
 
    override fun onUpdate(): ObjectA ? {
        return objectA
    }
 
    override fun onChanged(ref: ObjectA) {
        this@MainActivity.objectA = objectA
        // notify change on UI
    }
 
    override fun progress(value: Int) {
        Log.e("rotor", "loading " + path + " -> " + value + " %")
    }
})
```
Remove listener in server by calling `removeListener()`
```java
Database.removeListener(path);
```

License
-------
    Copyright 2018 Efraín Espada

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
