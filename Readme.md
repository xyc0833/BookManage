## 実践：Mybatis+JUL+Lombok+Mavenを基にした図書管理システム（単体テスト付き）
本システムはMybatisをデータアクセス層、JUL（Java Util Logging）で完全なログシステムを構築し、
Lombokでコードを簡素化、Mavenで依存とビルドを管理した図書管理システムです。

### 主な機能
1. 学生情報・書籍情報のオンライン登録
2. 書籍情報リストの検索
3. 学生情報リストの検索
4. 貸出情報リストの検索
5. 操作ログ・エラーログなどを含む完全なログ管理

全機能に対応する単体テストを付属し、コードの信頼性を保証しています。
初心者向けのJava実践プロジェクトとして、Mybatisの使い方やログ設計、Mavenプロジェクト構成を学べます。




## 学习记录

@Data
public class Student {
int sid;
final String name;
final String sex;
final int grade;
} 这里为什么要加 final 是不是和@data注解有关能否解释一下



你想弄明白这段代码里给 `name`、`sex`、`grade` 加 `final` 关键字，是否和 `@Data` 注解有关，我先帮你理清核心逻辑：

### 核心结论先明确
`final` 关键字**不是** `@Data` 注解强制要求的，两者没有直接的“必须绑定”关系；给字段加 `final` 是开发者的**设计选择**，但 `@Data` 注解生成的代码会和 `final` 字段产生联动，这是你需要重点理解的点。

### 一、先搞懂 `final` 字段的本质含义
`final` 修饰成员变量时，核心规则是：**字段必须在创建对象时完成初始化（赋值），且一旦赋值后就不能被修改**。
对 `Student` 类来说，给 `name`/`sex`/`grade` 加 `final`，意味着设计上认为：
- 学生的姓名、性别、年级是“不可变属性”（比如创建学生对象后，不允许修改姓名）；
- 这些字段必须在对象初始化时（构造方法、字段直接赋值、初始化块）确定值，后续无法通过任何方式修改。

### 二、`@Data` 注解和 `final` 字段的联动（关键）
`@Data` 是 Lombok 提供的注解，它会自动帮你生成：
1. 所有字段的 `getter` 方法；
2. 非 `final` 字段的 `setter` 方法；
3. `toString()`、`equals()`、`hashCode()` 方法；
4. 全参构造方法（如果有 `final` 字段时，这个规则会更严格）。

#### 关键联动点：
| 字段是否加 final | @Data 生成的代码差异 |
|------------------|----------------------|
| 不加 final（如 sid） | 生成 `getSid()` + `setSid()` |
| 加 final（如 name） | 只生成 `getName()`，**不会生成 `setName()`** |

这正好契合了 `final` 的设计初衷：既然字段不可修改，就没必要生成 setter 方法，Lombok 会自动适配这个规则。

### 三、代码示例：看 `@Data` 生成的实际代码
你写的 `Student` 类，Lombok 最终生成的等效 Java 代码如下（简化版）：
```java
public class Student {
    int sid;
    final String name;
    final String sex;
    final int grade;

    // 全参构造方法（因为有final字段，必须通过构造方法初始化）
    public Student(int sid, String name, String sex, int grade) {
        this.sid = sid;
        this.name = name;
        this.sex = sex;
        this.grade = grade;
    }

    // sid的getter + setter（非final）
    public int getSid() { return sid; }
    public void setSid(int sid) { this.sid = sid; }

    // final字段只有getter，无setter
    public String getName() { return name; }
    public String getSex() { return sex; }
    public int getGrade() { return grade; }

    // 自动生成toString/equals/hashCode（略）
}
```

#### 补充：如果 final 字段没初始化会怎样？
如果只加 `final` 但没赋值，比如：
```java
@Data
public class Student {
    final String name; // 只声明，未赋值
}
```
编译时会报错，因为 `final` 字段必须初始化，而 `@Data` 生成的全参构造方法是唯一的初始化途径（你也可以手动给字段赋值，比如 `final String name = "张三";`）。

### 总结
1. `final` 是**设计层面**的选择，用于标记“不可变字段”，和 `@Data` 注解无强制关联；
2. `@Data` 会适配 `final` 规则：给 final 字段只生成 getter，不生成 setter，同时强制生成包含 final 字段的全参构造方法；
3. 加 `final` 的核心目的是保证字段不可修改，而 `@Data` 只是根据这个修饰符调整了生成的代码逻辑。

简单说：你可以给字段加/不加 final，`@Data` 都能正常工作，只是生成的 setter 方法会有差异；加 final 是开发者想让这些字段“只读”，而非 `@Data` 要求这么做。


## SqlUtil的作用

你想了解这段 `SqlUtil` 工具类代码的具体作用，我先帮你概括核心功能：
这段代码是一个基于 MyBatis 框架的**工具类**，核心目的是**封装 SqlSession 的创建逻辑**，提供统一、便捷的方式获取数据库会话（SqlSession），同时通过单例模式保证 SqlSessionFactory 只初始化一次，提升程序性能。

### 一、逐行拆解代码功能（新手友好版）
我会按代码执行顺序，把每个部分的作用讲清楚：

#### 1. 私有构造方法：`private SqlUtil(){}`
```java
private SqlUtil(){ }
```
- **作用**：私有化构造方法，阻止外部通过 `new SqlUtil()` 创建这个类的对象。
- **为什么这么做**：这个类是工具类，核心逻辑都通过静态变量/方法实现，不需要创建实例，私有化构造能避免误用。

#### 2. 静态变量：`private static SqlSessionFactory factory;`
```java
private static SqlSessionFactory factory;
```
- **核心概念**：`SqlSessionFactory` 是 MyBatis 的“工厂类”，负责创建 `SqlSession`（数据库会话），它的创建成本很高（需要加载配置文件、解析映射关系）。
- **static 修饰**：保证整个程序运行期间，`factory` 只有一个实例（单例），避免重复创建导致性能损耗。

#### 3. 静态代码块：初始化 SqlSessionFactory
```java
static {
    try {
        // 加载 MyBatis 核心配置文件，创建 SqlSessionFactory
        factory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config.xml"));
    } catch (IOException e) {
        // 捕获配置文件加载失败的异常，转为运行时异常抛出
        throw new RuntimeException(e);
    }
}
```
- **执行时机**：静态代码块在**类第一次被加载时执行，且只执行一次**，正好契合 `SqlSessionFactory` “只初始化一次”的需求。
- **核心逻辑**：
    - `Resources.getResourceAsReader("mybatis-config.xml")`：从项目的类路径下读取 MyBatis 的核心配置文件（包含数据库连接信息、映射文件路径等）；
    - `SqlSessionFactoryBuilder().build(...)`：通过配置文件构建 `SqlSessionFactory` 实例；
    - 异常处理：如果配置文件找不到/读取失败，捕获 `IOException` 并转为运行时异常，让问题直接暴露，方便排查。

#### 4. 公共方法：获取 SqlSession
```java
public SqlSession getSession(){
    // 打开一个 SqlSession，参数 true 表示“自动提交事务”
    return factory.openSession(true);
}
```
- **核心概念**：`SqlSession` 是 MyBatis 操作数据库的核心对象，相当于数据库的“会话”，可以执行 SQL 语句、提交/回滚事务。
- **关键参数**：`factory.openSession(true)` 中 `true` 表示**自动提交事务**（默认是 false，需要手动调用 `session.commit()`）；
- **作用**：对外提供统一的获取 `SqlSession` 的入口，调用者不需要关心 `SqlSessionFactory` 的创建细节，直接调用 `getSession()` 就能拿到可用的数据库会话。

### 二、实际使用场景（帮你理解怎么用）
假设你要操作数据库，原本需要每次都加载配置、创建工厂、打开会话，现在只需要：
```java
// 1. 创建工具类对象（注意：这里其实有问题，后面会说）
SqlUtil sqlUtil = new SqlUtil(); 
// 2. 获取 SqlSession
SqlSession session = sqlUtil.getSession();
// 3. 用 session 执行 SQL（比如查询用户）
UserMapper mapper = session.getMapper(UserMapper.class);
User user = mapper.selectById(1);
```

### 三、代码的小问题（优化建议）
这段代码有个小瑕疵：`getSession()` 是普通成员方法，需要创建 `SqlUtil` 对象才能调用，但构造方法又被私有化了（外部根本new不出来）。
**正确的写法**应该把 `getSession()` 改成静态方法，这样更符合工具类的设计：
```java
// 修正后的 getSession 方法
public static SqlSession getSession(){
    return factory.openSession(true);
}

// 调用方式（无需创建对象）
SqlSession session = SqlUtil.getSession();
```

### 总结
1. 核心作用：**封装 MyBatis 初始化和 SqlSession 创建逻辑**，简化数据库操作的前置步骤，避免重复创建 SqlSessionFactory 造成性能浪费；
2. 关键设计：通过 `static` 代码块实现 SqlSessionFactory 单例初始化，私有化构造方法避免工具类被实例化；
3. 核心细节：`openSession(true)` 开启自动提交事务，调用者无需手动处理事务提交（适合简单的增删改查场景）。

简单来说，这个工具类把 MyBatis 最繁琐的“加载配置、创建工厂”步骤封装起来，让你只用一行代码就能拿到操作数据库的 SqlSession。


## 这个Consumer 是哪里来的？

你想知道代码中`Consumer`的来源和作用，我会从**类的位置、核心作用、使用逻辑**三个维度，用新手易懂的方式讲解，帮你彻底搞懂这个`Consumer`。

### 一、Consumer的来源：JDK内置的函数式接口
`Consumer`是Java标准库中**java.util.function**包下的一个**函数式接口**（JDK 8及以上版本新增），所以使用前必须先导入这个类：
```java
// 核心导入语句（缺一不可）
import java.util.function.Consumer;
```
如果不导入，代码会编译报错（找不到`Consumer`类）。

### 二、Consumer的核心作用（通俗理解）
`Consumer`直译是“消费者”，它的核心功能是：
> 接收一个参数（比如代码中的`BookMapper`），执行一段逻辑，但**没有返回值**。

简单说，它就像一个“操作模板”：你只需要告诉它“要对传入的对象做什么操作”，它就会执行对应的逻辑，核心方法是`accept(T t)`。

### 三、结合你的代码理解Consumer的使用逻辑
你的代码中`Consumer<BookMapper>`表示：这个“消费者”专门接收`BookMapper`类型的参数，执行和`BookMapper`相关的数据库操作（比如查询、新增）。

#### 1. 完整代码（补全导入+调用示例）
```java
// 必须导入Consumer
import java.util.function.Consumer;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

public class BookDao {
    // 假设factory是已初始化的SqlSessionFactory
    private static SqlSessionFactory factory;

    /**
     * 执行与图书相关的数据库操作的通用方法
     * @param consumer 接收BookMapper，执行具体的数据库操作（无返回值）
     */
    public static void doSqlWork(Consumer<BookMapper> consumer){
        // try-with-resources自动关闭SqlSession，openSession(true)开启自动提交
        try(SqlSession sqlSession = factory.openSession(true)){
            BookMapper bookMapper = sqlSession.getMapper(BookMapper.class);
            // 执行consumer中定义的操作（调用方自定义）
            consumer.accept(bookMapper);
        }
    }

    // ========== 调用示例：用Consumer指定具体操作 ==========
    public static void main(String[] args) {
        // 示例1：查询所有书籍（Consumer接收BookMapper，执行listBooks()）
        doSqlWork(bookMapper -> {
            // 这里的逻辑就是Consumer要“消费”BookMapper做的事
            bookMapper.listBooks(); // 假设BookMapper有listBooks方法
        });

        // 示例2：新增书籍（Consumer执行insertBook）
        doSqlWork(bookMapper -> {
            Book book = new Book(1, "Java编程", "张三");
            bookMapper.insertBook(book); // 假设BookMapper有insertBook方法
        });
    }
}
```

#### 2. 关键拆解
- **定义方法时**：`doSqlWork(Consumer<BookMapper> consumer)` 表示“这个方法需要接收一个‘处理BookMapper的逻辑’”；
- **调用方法时**：通过Lambda表达式（`bookMapper -> { ... }`）告诉`Consumer`“具体要对BookMapper做什么”；
- **核心解耦**：`doSqlWork`只负责“创建SqlSession、获取Mapper、关闭资源”这些通用逻辑，而“具体查什么、新增什么”由调用方通过`Consumer`自定义，不用重复写通用代码。

### 四、Consumer的核心方法（新手必记）
`Consumer`只有一个抽象方法：
```java
// 接收一个参数t，执行操作，无返回值
void accept(T t);
```
你的代码中`consumer.accept(bookMapper)`就是调用这个方法，把`BookMapper`传给调用方定义的逻辑。

### 五、为什么用Consumer？（新手理解优势）
如果不用`Consumer`，你需要为每个数据库操作写重复的代码：
```java
// 不用Consumer的弊端：重复代码多
public static void listBooks() {
    try(SqlSession sqlSession = factory.openSession(true)){
        BookMapper bookMapper = sqlSession.getMapper(BookMapper.class);
        bookMapper.listBooks(); // 重复的通用逻辑+具体操作
    }
}

public static void insertBook(Book book) {
    try(SqlSession sqlSession = factory.openSession(true)){
        BookMapper bookMapper = sqlSession.getMapper(BookMapper.class);
        bookMapper.insertBook(book); // 又写一遍通用逻辑
    }
}
```
而用`Consumer`后，通用逻辑只写一次，具体操作通过`Consumer`传入，大幅减少重复代码。

### 总结
`Consumer`的核心关键点：
1. **来源**：`java.util.function.Consumer`，JDK 8+内置，需导入才能用；
2. **作用**：接收一个参数，执行逻辑，无返回值（“消费”参数）；
3. **你的代码中**：用来传递“对BookMapper的具体操作”，解耦通用逻辑和业务逻辑；
4. **优势**：避免重复写SqlSession创建/关闭的代码，简化多数据库操作的实现。

如果想进一步了解`Consumer`和其他函数式接口（比如`Function`有返回值、`Supplier`无参数）的区别，我可以补充对比说明！


## 出现报错

```bash
/Library/Java/JavaVirtualMachines/zulu-21.jdk/Contents/Home/bin/java -javaagent:/Users/xuyaochen/Library/Caches/JetBrains/IntelliJIdea2025.3/captureAgent/debugger-agent.jar=file:///var/folders/_g/7893c3xj2dj40fx6k2bwhc3m0000gn/T/capture8226607154169222907.props -ea -Didea.test.cyclic.buffer.size=1048576 -javaagent:/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar=51849 -Dkotlinx.coroutines.debug.enable.creation.stack.trace=false -Ddebugger.agent.enable.coroutines=true -Dkotlinx.coroutines.debug.enable.flows.stack.trace=true -Dkotlinx.coroutines.debug.enable.mutable.state.flows.stack.trace=true -Ddebugger.async.stack.trace.for.all.threads=true -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -classpath /Users/xuyaochen/.m2/repository/org/junit/platform/junit-platform-launcher/1.8.1/junit-platform-launcher-1.8.1.jar:/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar:/Applications/IntelliJ IDEA.app/Contents/plugins/junit/lib/junit6-rt.jar:/Applications/IntelliJ IDEA.app/Contents/plugins/junit/lib/junit5-rt.jar:/Applications/IntelliJ IDEA.app/Contents/plugins/junit/lib/junit-rt.jar:/Users/xuyaochen/FuRiIT/project/BookManage/target/test-classes:/Users/xuyaochen/FuRiIT/project/BookManage/target/classes:/Users/xuyaochen/.m2/repository/org/projectlombok/lombok/1.18.30/lombok-1.18.30.jar:/Users/xuyaochen/.m2/repository/org/junit/jupiter/junit-jupiter/5.8.1/junit-jupiter-5.8.1.jar:/Users/xuyaochen/.m2/repository/org/junit/jupiter/junit-jupiter-api/5.8.1/junit-jupiter-api-5.8.1.jar:/Users/xuyaochen/.m2/repository/org/opentest4j/opentest4j/1.2.0/opentest4j-1.2.0.jar:/Users/xuyaochen/.m2/repository/org/junit/platform/junit-platform-commons/1.8.1/junit-platform-commons-1.8.1.jar:/Users/xuyaochen/.m2/repository/org/apiguardian/apiguardian-api/1.1.2/apiguardian-api-1.1.2.jar:/Users/xuyaochen/.m2/repository/org/junit/jupiter/junit-jupiter-params/5.8.1/junit-jupiter-params-5.8.1.jar:/Users/xuyaochen/.m2/repository/org/junit/jupiter/junit-jupiter-engine/5.8.1/junit-jupiter-engine-5.8.1.jar:/Users/xuyaochen/.m2/repository/org/junit/platform/junit-platform-engine/1.8.1/junit-platform-engine-1.8.1.jar:/Users/xuyaochen/.m2/repository/mysql/mysql-connector-java/8.0.27/mysql-connector-java-8.0.27.jar:/Users/xuyaochen/.m2/repository/com/google/protobuf/protobuf-java/3.11.4/protobuf-java-3.11.4.jar:/Users/xuyaochen/.m2/repository/org/mybatis/mybatis/3.5.7/mybatis-3.5.7.jar com.intellij.rt.junit.JUnitStarter -ideVersion5 -junit5 com.test.MainTest,test1

org.apache.ibatis.exceptions.PersistenceException: 
### Error querying database.  Cause: org.apache.ibatis.executor.result.ResultMapException: Error attempting to get column 'sex' from result set.  Cause: java.sql.SQLDataException: Cannot determine value type from string '男'
### The error may exist in book/manage/mapper/BookMapper.java (best guess)
### The error may involve book.manage.mapper.BookMapper.getStudentBySid
### The error occurred while handling results
### SQL: select * from student where sid = ?
### Cause: org.apache.ibatis.executor.result.ResultMapException: Error attempting to get column 'sex' from result set.  Cause: java.sql.SQLDataException: Cannot determine value type from string '男'

	at org.apache.ibatis.exceptions.ExceptionFactory.wrapException(ExceptionFactory.java:30)
	at org.apache.ibatis.session.defaults.DefaultSqlSession.selectList(DefaultSqlSession.java:153)
	at org.apache.ibatis.session.defaults.DefaultSqlSession.selectList(DefaultSqlSession.java:145)
	at org.apache.ibatis.session.defaults.DefaultSqlSession.selectList(DefaultSqlSession.java:140)
	at org.apache.ibatis.binding.MapperMethod.executeForMany(MapperMethod.java:147)
	at org.apache.ibatis.binding.MapperMethod.execute(MapperMethod.java:80)
	at org.apache.ibatis.binding.MapperProxy$PlainMethodInvoker.invoke(MapperProxy.java:145)
	at org.apache.ibatis.binding.MapperProxy.invoke(MapperProxy.java:86)
	at jdk.proxy1/jdk.proxy1.$Proxy15.getBorrowList(Unknown Source)
	at com.test.MainTest.lambda$test1$0(MainTest.java:10)
	at book.manage.sql.SqlUtil.doSqlWork(SqlUtil.java:36)
	at com.test.MainTest.test1(MainTest.java:9)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)
	at java.base/java.lang.reflect.Method.invoke(Method.java:580)
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:725)
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131)
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:149)
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:140)
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:84)
	at org.junit.jupiter.engine.execution.ExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(ExecutableInvoker.java:115)
	at org.junit.jupiter.engine.execution.ExecutableInvoker.lambda$invoke$0(ExecutableInvoker.java:105)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37)
	at org.junit.jupiter.engine.execution.ExecutableInvoker.invoke(ExecutableInvoker.java:104)
	at org.junit.jupiter.engine.execution.ExecutableInvoker.invoke(ExecutableInvoker.java:98)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$7(TestMethodTestDescriptor.java:214)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:210)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:135)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:66)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:151)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35)
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57)
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:107)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:88)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:54)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:67)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:52)
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:114)
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:86)
	at org.junit.platform.launcher.core.DefaultLauncherSession$DelegatingLauncher.execute(DefaultLauncherSession.java:86)
	at org.junit.platform.launcher.core.SessionPerRequestLauncher.execute(SessionPerRequestLauncher.java:53)
	at com.intellij.junit5.JUnit5IdeaTestRunner.startRunnerWithArgs(JUnit5IdeaTestRunner.java:66)
	at com.intellij.rt.junit.IdeaTestRunner$Repeater$1.execute(IdeaTestRunner.java:38)
	at com.intellij.rt.execution.junit.TestsRepeater.repeat(TestsRepeater.java:11)
	at com.intellij.rt.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:35)
	at com.intellij.rt.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:237)
	at com.intellij.rt.junit.JUnitStarter.main(JUnitStarter.java:58)
Caused by: org.apache.ibatis.executor.result.ResultMapException: Error attempting to get column 'sex' from result set.  Cause: java.sql.SQLDataException: Cannot determine value type from string '男'
	at org.apache.ibatis.type.BaseTypeHandler.getResult(BaseTypeHandler.java:87)
	at org.apache.ibatis.executor.resultset.DefaultResultSetHandler.createUsingConstructor(DefaultResultSetHandler.java:711)
	at org.apache.ibatis.executor.resultset.DefaultResultSetHandler.createByConstructorSignature(DefaultResultSetHandler.java:694)
	at org.apache.ibatis.executor.resultset.DefaultResultSetHandler.createResultObject(DefaultResultSetHandler.java:658)
	at org.apache.ibatis.executor.resultset.DefaultResultSetHandler.createResultObject(DefaultResultSetHandler.java:631)
	at org.apache.ibatis.executor.resultset.DefaultResultSetHandler.getRowValue(DefaultResultSetHandler.java:398)
	at org.apache.ibatis.executor.resultset.DefaultResultSetHandler.handleRowValuesForSimpleResultMap(DefaultResultSetHandler.java:355)
	at org.apache.ibatis.executor.resultset.DefaultResultSetHandler.handleRowValues(DefaultResultSetHandler.java:329)
	at org.apache.ibatis.executor.resultset.DefaultResultSetHandler.handleResultSet(DefaultResultSetHandler.java:302)
	at org.apache.ibatis.executor.resultset.DefaultResultSetHandler.handleResultSets(DefaultResultSetHandler.java:195)
	at org.apache.ibatis.executor.statement.PreparedStatementHandler.query(PreparedStatementHandler.java:65)
	at org.apache.ibatis.executor.statement.RoutingStatementHandler.query(RoutingStatementHandler.java:79)
	at org.apache.ibatis.executor.SimpleExecutor.doQuery(SimpleExecutor.java:63)
	at org.apache.ibatis.executor.BaseExecutor.queryFromDatabase(BaseExecutor.java:325)
	at org.apache.ibatis.executor.BaseExecutor.query(BaseExecutor.java:156)
	at org.apache.ibatis.executor.CachingExecutor.query(CachingExecutor.java:109)
	at org.apache.ibatis.executor.loader.ResultLoader.selectList(ResultLoader.java:81)
	at org.apache.ibatis.executor.loader.ResultLoader.loadResult(ResultLoader.java:70)
	at org.apache.ibatis.executor.resultset.DefaultResultSetHandler.getNestedQueryMappingValue(DefaultResultSetHandler.java:800)
	at org.apache.ibatis.executor.resultset.DefaultResultSetHandler.getPropertyMappingValue(DefaultResultSetHandler.java:505)
	at org.apache.ibatis.executor.resultset.DefaultResultSetHandler.applyPropertyMappings(DefaultResultSetHandler.java:481)
	at org.apache.ibatis.executor.resultset.DefaultResultSetHandler.getRowValue(DefaultResultSetHandler.java:405)
	at org.apache.ibatis.executor.resultset.DefaultResultSetHandler.handleRowValuesForSimpleResultMap(DefaultResultSetHandler.java:355)
	at org.apache.ibatis.executor.resultset.DefaultResultSetHandler.handleRowValues(DefaultResultSetHandler.java:329)
	at org.apache.ibatis.executor.resultset.DefaultResultSetHandler.handleResultSet(DefaultResultSetHandler.java:302)
	at org.apache.ibatis.executor.resultset.DefaultResultSetHandler.handleResultSets(DefaultResultSetHandler.java:195)
	at org.apache.ibatis.executor.statement.PreparedStatementHandler.query(PreparedStatementHandler.java:65)
	at org.apache.ibatis.executor.statement.RoutingStatementHandler.query(RoutingStatementHandler.java:79)
	at org.apache.ibatis.executor.SimpleExecutor.doQuery(SimpleExecutor.java:63)
	at org.apache.ibatis.executor.BaseExecutor.queryFromDatabase(BaseExecutor.java:325)
	at org.apache.ibatis.executor.BaseExecutor.query(BaseExecutor.java:156)
	at org.apache.ibatis.executor.CachingExecutor.query(CachingExecutor.java:109)
	at org.apache.ibatis.executor.CachingExecutor.query(CachingExecutor.java:89)
	at org.apache.ibatis.session.defaults.DefaultSqlSession.selectList(DefaultSqlSession.java:151)
	... 77 more
Caused by: java.sql.SQLDataException: Cannot determine value type from string '男'
	at com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(SQLError.java:114)
	at com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(SQLError.java:97)
	at com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(SQLError.java:89)
	at com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(SQLError.java:63)
	at com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(SQLError.java:73)
	at com.mysql.cj.jdbc.exceptions.SQLExceptionsMapping.translateException(SQLExceptionsMapping.java:96)
	at com.mysql.cj.jdbc.result.ResultSetImpl.getObject(ResultSetImpl.java:1423)
	at com.mysql.cj.jdbc.result.ResultSetImpl.getInt(ResultSetImpl.java:822)
	at com.mysql.cj.jdbc.result.ResultSetImpl.getInt(ResultSetImpl.java:843)
	at org.apache.ibatis.type.IntegerTypeHandler.getNullableResult(IntegerTypeHandler.java:37)
	at org.apache.ibatis.type.IntegerTypeHandler.getNullableResult(IntegerTypeHandler.java:26)
	at org.apache.ibatis.type.BaseTypeHandler.getResult(BaseTypeHandler.java:85)
	... 110 more
Caused by: com.mysql.cj.exceptions.DataConversionException: Cannot determine value type from string '男'
	at com.mysql.cj.result.AbstractNumericValueFactory.createFromBytes(AbstractNumericValueFactory.java:65)
	at com.mysql.cj.protocol.a.MysqlTextValueDecoder.decodeByteArray(MysqlTextValueDecoder.java:143)
	at com.mysql.cj.protocol.result.AbstractResultsetRow.decodeAndCreateReturnValue(AbstractResultsetRow.java:135)
	at com.mysql.cj.protocol.result.AbstractResultsetRow.getValueFromBytes(AbstractResultsetRow.java:243)
	at com.mysql.cj.protocol.a.result.ByteArrayRow.getValue(ByteArrayRow.java:91)
	at com.mysql.cj.jdbc.result.ResultSetImpl.getObject(ResultSetImpl.java:1316)
	... 115 more


进程已结束，退出代码为 255


```


你原来的`Student`实体类里`sex`虽然是`String`类型（匹配数据库的“男/女”），但核心问题出在**final修饰的字段没有对应的构造方法**，
且**缺少setter方法**，导致MyBatis无法实例化`Student`对象、无法给字段赋值，最终抛出类型转换异常（表象是“无法识别‘男’”，本质是实例化失败）。

我会先分析问题根源，再给出完整的修复代码，确保MyBatis能正常映射。

### 一、核心问题根源（关键！）
MyBatis实例化实体类的逻辑：
1. 优先找**无参构造方法**，通过`setter`给字段赋值；
2. 若无无参构造，会找**匹配查询结果列的有参构造**，通过构造方法赋值；

你的`Student`类存在两个致命问题：
#### 1. 所有字段（除sid）都用`final`修饰，且无对应构造方法
- `final`字段必须在**构造方法中初始化**，但你的类没有自定义构造方法，只有JVM默认的无参构造；
- 无参构造无法初始化`final`字段，MyBatis反射实例化时会直接失败，进而抛出“无法确定值类型”的异常（表象错误）。

#### 2. 缺少getter/setter方法
MyBatis默认通过`setter`给字段赋值，但你的类没有任何`setter`，即使去掉`final`，也无法给字段赋值。

### 二、完整修复后的Student类
保留`final`字段的同时，添加**全参构造方法**（MyBatis能通过构造方法赋值），并补充`getter`（便于后续获取字段值）：
```java
package book.manage.entity;

// 若用Lombok，可直接加@AllArgsConstructor + @Getter，无需手动写构造和getter
public class Student {
    int sid; // 非final，可通过setter赋值（或也加final，放到构造里）
    final String name;
    final String sex;
    final int grade;

    // 核心：添加全参构造方法，初始化所有final字段
    // MyBatis会根据查询结果的列名匹配构造方法的参数名（需保证参数名和列名一致）
    public Student(int sid, String name, String sex, int grade) {
        this.sid = sid;
        this.name = name;
        this.sex = sex;
        this.grade = grade;
    }

    // 补充：若sid也想设为final，把构造方法改成包含sid即可
    // public Student(String name, String sex, int grade) {
    //     this.name = name;
    //     this.sex = sex;
    //     this.grade = grade;
    // }

    // 必须添加getter（MyBatis关联查询后，能获取字段值；打印对象时也能看到值）
    public int getSid() {
        return sid;
    }

    // final字段无需setter，非final的sid可加setter（可选）
    public void setSid(int sid) {
        this.sid = sid;
    }

    public String getName() {
        return name;
    }

    public String getSex() {
        return sex;
    }

    public int getGrade() {
        return grade;
    }

    // 可选：重写toString，便于打印对象时看到具体值
    @Override
    public String toString() {
        return "Student{" +
                "sid=" + sid +
                ", name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", grade=" + grade +
                '}';
    }
}
```

### 三、辅助修复：BookMapper中补充构造方法映射（可选）
如果MyBatis仍无法匹配构造方法参数，可在`getStudentBySid`方法中添加`@ConstructorArgs`注解，明确列和构造参数的映射：
```java
// 在BookMapper中补充该注解，精准映射列和构造参数
@Select("select sid, name, sex, grade from student where sid = #{sid}")
@ConstructorArgs({
        @Arg(column = "sid", javaType = int.class),
        @Arg(column = "name", javaType = String.class),
        @Arg(column = "sex", javaType = String.class),
        @Arg(column = "grade", javaType = int.class)
})
Student getStudentBySid(int sid);
```

### 四、简化方案（推荐新手）
如果不需要`final`字段，直接去掉`final`，并添加无参构造+getter/setter（MyBatis最兼容的写法）：
```java
package book.manage.entity;

public class Student {
    int sid;
    String name;
    String sex;
    int grade;

    // 无参构造（必须）
    public Student() {}

    // getter/setter（必须）
    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    @Override
    public String toString() {
        return "Student{" +
                "sid=" + sid +
                ", name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", grade=" + grade +
                '}';
    }
}
```

### 总结
核心修复点：
1. 去掉`final`（新手推荐），或给`final`字段添加**全参构造方法**；
2. 必须添加`getter`（MyBatis需要读取字段值），无参构造（MyBatis默认实例化）；
3. 异常的本质是“`final`字段无法初始化导致实例化失败”，而非“类型不匹配”（表象）。

修复后，再运行`MainTest`的`test1`方法，就能正常查询到`borrow`列表并关联学生/书籍信息了。