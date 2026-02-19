package book.manage;

import book.manage.entity.Book;
import book.manage.entity.Student;
import book.manage.mapper.BookMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
//        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config.xml"));
//        try(SqlSession sqlSession = factory.openSession(true)){
//            BookMapper mapper = sqlSession.getMapper(BookMapper.class);
//            System.out.println(mapper.addstudent(new Student("xyc01","男",2020)));
//            System.out.println(mapper.addbook(new Book("书籍2","相关信息",20.00)));
//        }

        try(Scanner scanner = new Scanner(System.in)){
            while(true){
                System.out.println("====================");
                System.out.println("1. 录入学生信息");
                System.out.println("2. 录入书籍信息");
                System.out.print("输入您想要执行的操作(输入其他任意数字退出): ");
                int input;
                try{
                    input = scanner.nextInt();
                }catch (Exception e){
                    return;
                }
                scanner.nextLine();
                switch (input){
                    case 1:
                        break;
                    case 2:
                        break;
                    default:
                        return;
                }
            }
        }
    }
}
