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