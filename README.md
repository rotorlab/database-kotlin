<p align="center"><img width="30%" vspace="20" src="https://github.com/rotorlab/database-kotlin/raw/develop/app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png"></p>

# Rotor Database



### Usage
- Import library:

```groovy
android {
    defaultConfig {
        multiDexEnabled true
    }
}
 
dependencies {
    implementation 'com.rotor:database:0.0.1'
    implementation 'com.efraespada:jsondiff:1.1.0'
    implementation 'com.squareup.retrofit2:retrofit:2.3.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.3.0'
    implementation 'com.google.code.findbugs:jsr305:2.0.1'
    implementation 'com.google.guava:guava:22.0-android'
    implementation 'com.google.code.gson:gson:2.8.2'
}
```
- Initialize database module after Rotor initialization:
```java
Database.initialize()
```
Listen object changes:
```java
ObjectA objectA = null;
  
Rotor.listener(path, new Reference<ObjectA>() {
    
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
 
});
```
Remove listener in server by calling `removeListener()`
```java
Rotor.removeListener(path);
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
    
    Rotor.listener(path, new Reference<Chat>() {
    
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
    
    });
     
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
