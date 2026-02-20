package book.manage.sql;

import book.manage.mapper.BookMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.util.function.Consumer;

public class SqlUtil {
    private SqlUtil(){

    }
    private static SqlSessionFactory factory;
    static {
        try {
            factory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config.xml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 执行与图书相关的数据库操作的通用方法
     * 封装SqlSession的创建、Mapper获取、资源自动关闭等通用逻辑，简化业务代码
     * @param consumer 函数式接口，接收BookMapper对象，用于执行具体的数据库操作（如增删改查）
     */
    public static void doSqlWork(Consumer<BookMapper> consumer){
        // 1. try-with-resources语法：自动关闭SqlSession（实现AutoCloseable接口），无需手动close
        // 2. factory.openSession(true)：创建SqlSession时指定自动提交事务（参数true），无需手动commit
        try(SqlSession sqlSession = factory.openSession(true)){
            // 获取BookMapper接口的代理对象，Mybatis会自动实现接口并映射SQL
            BookMapper bookMapper = sqlSession.getMapper(BookMapper.class);
            // 执行传入的具体数据库操作（由调用方自定义，比如查询书籍、新增学生等）
            consumer.accept(bookMapper);
        }
        // try块结束后，SqlSession会自动关闭，释放数据库连接；若发生异常，事务会自动回滚（因自动提交开启）
    }
}
