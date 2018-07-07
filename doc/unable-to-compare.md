## Rules that cannot be matched using existed comparison algorithm

#### 1. Left recursive rules

_Examples:_

```kotlin
annotation class ForHttpMethod(val method: io.vertx.core.http.HttpMethod)
```
<img src=images/left-recursion-1.png width=700>

```kotlin
package org.jetbrains.research.kotoed
```
<img src=images/left-recursion-2.png width=700>

#### 2. PostfixUnaryExpressions with postfixUnarySuffix

_Example:_

```kotlin
class UavUser() : User, Loggable, ClusterSerializable {
    constructor(vertx: Vertx, denizenId: String, id: Int) : this() {
        this.vertx = vertx
        this.denizenId = denizenId
        this.id = id
    }
}
```
<img src=images/suffix.png width=700>

#### 3. EnumClassBody

_Example:_

```kotlin
enum class CompareOp(val rep: String) {
    EQ,
    NE,
    GT,
    // ...
}
```
<img src=images/enum.png width=700>

#### 4. Annotated or labeled

_Examples:_

```kotlin
@Deprecated()
fun a() = 1
```
<img src=images/annotated.png width=700>

```kotlin
fun a() {
    loop@ expr
}
```
<img src=images/labeled.png width=700>

#### 5. CompanionObject/EnumClass modifiers

_Example:_

```kotlin
protected companion object {}
```
<img src=images/modifiers.png width=700>
