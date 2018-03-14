
# Rotor Database

Work with synchronized java objects stored as JSON objects. Check a sample above (Chappy app).

### What is this?
Flamebase is an open source project that tries to emulate Firebase Database features as much as possible. In this repo you can find the proper lib for android client.
For now it still developing, so please be patient with errors.

### Requirements
**1º redis-server:** Amazing Pub/Sub engine for real-time changes. Simply install and start it.

**2º flamebase-server:** It will be our server cluster for storing json objects. Server cluster is run with **node** framework.

Check out [flamebase-server repo](https://github.com/flamebase/flamebase-server) for more information.

### Usage
- Import library:

```groovy
android {
 
    defaultConfig {
        multiDexEnabled true
    }
    
}
 
dependencies {
    implementation 'com.flamebase:database:1.6.1'
    implementation 'com.efraespada:jsondiff:1.1.0'
    implementation 'com.squareup.retrofit2:retrofit:2.3.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.3.0'
    implementation 'com.google.code.findbugs:jsr305:2.0.1'
    implementation 'com.google.guava:guava:22.0-android'
    implementation 'com.google.code.gson:gson:2.8.2'
}
```
- Initialize library:
```java
// redis ips starts with redis://, port is not included
FlamebaseDatabase.initialize(getApplicationContext(), "http://10.0.2.2:1507/", "redis://10.0.2.2", new StatusListener() {
 
    @Override
    public void connected() {
        /* fired only when initialized method is called and library is connected to redis */
    }
    
    @Override
    public void reconnecting() {
        /* library is trying to connect to redis */
    }
 
});
 
// debug logs
FlamebaseDatabase.setDebug(true);
```
- Listener for objects:
```java
ObjectA objectA = null;
  
FlamebaseDatabase.listener(path, new ObjectBlower<ObjectA>() {
    
    /**
    * called when listener is created on server, there is nothing stored
    * on db on the given path and onUpdate() still returning null
    */
    @Override
    public void onCreate() {
        objectA = new ObjectA();
        objectA.setValue("foo");
        
        // sync with server
        FlamebaseDatabase.sync(path);
    }
    
    /**
    * called after reference is synchronized with server
    * or is ready to be used.
    */
    @Override
    public void onChanged(ObjectA ref) {
        objectA = ref;  
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
 
}, ObjectA.class);
```
- Listener for maps:
```java
Map<String, Member> contacts = null;
 
FlamebaseDatabase.listener(path, new MapBlower<Member>() {
   
    @Override
    public void onCreate() {
        contacts = new HashMap<String, Member>();
        // add a member
        
        // sync with server
        FlamebaseDatabase.sync(path);
    }
    
    @Override
    public void onChanged(Map<String, Member> ref) {
        contacts = ref;
    }
    
    @Override
    public Map<String, Member> onUpdate() {
        return contacts;
    }
 
    @Override
    public void progress(int value) {
        
    }
  
}, Member.class);
```
- Remove listener in server by calling:
```java
FlamebaseDatabase.removeListener(path);
```

Background updates (not optional)
------------------
Flamebase Database library works in background in order to receive updates when application is on background or foreground. You must add FlamebaseService to your `AndroidManifest.xml` file:
```xml
<application>
 
    <service
        android:name="com.flamebase.database.FlamebaseService"
        android:enabled="true"
        android:exported="true" />
 
</application>
```
This service is controlled when the application is present and must be `bind` or `unbind`. Add in activities:
```java
@Override
protected void onResume() {
    super.onResume();
    FlamebaseDatabase.onResume();
}
 
@Override
protected void onPause() {
    FlamebaseDatabase.onPause();
    super.onPause();
}
```
In the sample app chats still receiving updates on background, when the application is reopened there is no need to ask for updates.

Limitations 
-----------
**List objects aren't supported:** List objects gives problems when differences are being generated. If you plan to store iterations you can dispose a map object with the object types you want:
```json
{
    "0": 1,
    "1": "item2",
    "2": {
        "title": "title 3",
        "body": "body 3",
        "delay": 3
    }
}
```

Kotlin support
--------------
Flamebase Database works with Kotlin applications too. You must use `KotlinBlowers` to solve Gson deserialization problem:
```kotlin
data class Chat(@SerializedName("id") val id: String,
                @SerializedName("name") val name: String,
                @SerializedName("creationDate") val creationDate: Long,
                @SerializedName("members") val members: Map<String, Member>)
 
FlamebaseDatabase.listener(path, object : KotlinObjectBlower<Chat>() {
 
    override fun onCreate() {
        
    }
 
    override fun source(value: String?) {
        val gson = Gson()
        profile = gson.fromJson(value, Data.Profile::class.java)
    }
 
    override fun string(): String? {
        if (profile == null) {
            return null
        } else {
            val gson = Gson()
            gson.toJson(profile)
        }
    }
    
    override fun progress(value: Int) {
        /* */
    }
 
}, Chat::class.java)
```

Chappy: quick sample of real-time changes
-------------------------------------------
<p align="center"><img width="10%" vspace="20" src="https://github.com/flamebase/flamebase-database-android/raw/develop/app/src/main/res/mipmap-xxxhdpi/ic_launcher_rounded.png"></p>
 
Imagine define some simple objects and share it between other devices by paths (`/chats/welcome_chat`):
 
```java
public class Chat {

    @SerializedName("name")
    @Expose
    String name;

    @SerializedName("members")
    @Expose
    Map<String, Member> members;

    @SerializedName("messages")
    @Expose
    Map<String, Message> messages;

    public Chat(String name, Map<String, Member> members, Map<String, Message> messages) {
        this.name = name;
        this.members = members;
        this.messages = messages;
    }
    
    /* getter and setter methods */
    
}
```
Define a chat listener and add messages:
```java
private Chat chat;

@Override protected void onCreate(Bundle savedInstanceState) {
    
    final String path = "/chats/welcome_chat";
    
    /* object instances, list adapter, etc.. */
    
    FlamebaseDatabase.listener(path, new ObjectBlower<Chat>() {
    
        @Override public void onCreate() {
            chat = new Chat();
            chat.setTitle("Foo Chat");
            
            // sync with server
            FlamebaseDatabase.sync(path);
        }
            
        @Override public Chat onUpdate() {
            return chat;
        }
    
        @Override public void onChanged(Chat ref) {
            chat = ref;
            
            // update screent title
            ChatActivity.this.setTitle(chat.getName());
            
            // order messages
            Map<String, Message> messageMap = new TreeMap<>(new Comparator<String>() {
                @Override public int compare(String o1, String o2) {
                    Long a = Long.valueOf(o1);
                    Long b = Long.valueOf(o2);
                    if (a > b) {
                        return 1;
                    } else if (a < b) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
            messageMap.putAll(chat.getMessages());
            chat.setMessages(messageMap);
    
            // update list
            messageList.getAdapter().notifyDataSetChanged();
            messageList.smoothScrollToPosition(0);
        }
    
        @Override public void progress(int value) {
            // print progress
        }
    
    }, Chat.class);
     
    sendButton.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
            SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
            String username = prefs.getString("username", null);
            if (name != null) {
                Message message = new Message(username, messageText.getText().toString());
                chat.getMessages().put(String.valueOf(new Date().getTime()), message);
        
                FlamebaseDatabase.sync(path);
        
                messageText.setText("");
            }
        }
    });
}
```
You can do changes or wait for them. All devices listening the same object will receive this changes to stay up to date:
 
<p align="center"><img width="30%" vspace="20" src="https://github.com/flamebase/flamebase-database-android/raw/develop/sample1.png"></p>


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
