[ ![Download](https://api.bintray.com/packages/efff/maven/RotorKotlinDatabase/images/download.svg) ](https://bintray.com/efff/maven/RotorKotlinDatabase/_latestVersion)

<p align="center"><img width="10%" vspace="20" src="https://github.com/rotorlab/database-kotlin/raw/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png"></p>
<p align="center">Rotor Database Android library</p>

-----------------------------
> Before use this library, [Rotor Core](https://github.com/rotorlab/core-kotlin) must be initialized. Lastest version is always the same for all Rotor libs.
 
[Wiki](https://github.com/rotorlab/database-kotlin/wiki)

[Get Started](https://github.com/rotorlab/database-kotlin/wiki/Get-Started)

[Real Time Database](https://github.com/rotorlab/database-kotlin/wiki/Real-time-database)
 
Rotor Database is a complementary module for Rotor Core. It allows to work with shared (Java) objects between many devices offering users real time changes and better mobile data consumption. 

Forget things like swipe-to-refresh events, lots of server requests and object storage management. 

**Rotor Database philosophy** states that the only needed requests are those that change data on remote database. That means that the rest of requests you are imaging (give me updates, give updates, give me updates) are removed now.
 
<p align="center"><img width="40%" vspace="20" src="https://github.com/rotorlab/core-kotlin/blob/develop/core_implementation.png"></p>
 
Rotor Core is connected to Rotor and Redis servers. The first one controls object sharing queues, devices waiting for changes and all data edition on remote database. The second (as you probably know) gives us Pub/Sub messaging pattern for data changes replication.

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
