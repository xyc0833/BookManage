package book.manage;

import book.manage.entity.Book;
import book.manage.entity.Student;
import book.manage.mapper.BookMapper;
import book.manage.sql.SqlUtil;
import lombok.extern.java.Log;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.sql.SQLOutput;
import java.util.Scanner;
import java.util.logging.LogManager;

@Log
public class Main {
    public static void main(String[] args) throws IOException {
//        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config.xml"));
//        try(SqlSession sqlSession = factory.openSession(true)){
//            BookMapper mapper = sqlSession.getMapper(BookMapper.class);
//            System.out.println(mapper.addstudent(new Student("xyc01","男",2020)));
//            System.out.println(mapper.addbook(new Book("书籍2","相关信息",20.00)));
//        }


        try(Scanner scanner = new Scanner(System.in)){
            LogManager manager = LogManager.getLogManager();
            manager.readConfiguration(Resources.getResourceAsStream("logging.properties"));
            while(true){
                System.out.println("====================");
                System.out.println("1. 录入学生信息");
                System.out.println("2. 录入书籍信息");
                System.out.println("3. 查阅借阅信息");
                System.out.println("4. 添加借阅信息");
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
                        addStudent(scanner);
                        break;
                    case 2:
                        addBook(scanner);
                        break;
                    case 3:
                        showBorrow();
                        break;
                    case 4:
                        addBorrow(scanner);
                        break;
                    default:
                        return;
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    //添加查阅信息
    private static void showBorrow(){
        SqlUtil.doSqlWork(mapper -> {
            mapper.getBorrowList().forEach(borrow -> {
                System.out.println(borrow.getStudent().getName() + "->" + borrow.getBook().getTitle());
            });
        });
    }


    //添加借阅信息
    private  static  void addBorrow(Scanner scanner){
        //代码可以改进的地方 输入的内容不合法怎么办？
        System.out.println("请输入书籍号：");
        String a = scanner.nextLine();
        int bid = Integer.parseInt(a);
        System.out.println("请输入学号：");
        String b = scanner.nextLine();
        int sid = Integer.parseInt(b);
        SqlUtil.doSqlWork(mapper -> {
            int i = mapper.addBorrow(sid,bid);
            if(i>0){
                System.out.println("插入成功");
                log.info("新添加了一条借阅信息" + "书籍号:" + bid + "学号：" + sid  );
            }else{
                System.out.println("插入失败");
            }
        });

    }
    //添加书籍信息
    private static void addBook(Scanner scanner){
        //代码可以改进的地方 输入的内容不合法怎么办？
        //加入try catch
        System.out.println("请输入书籍标题");
        String title = scanner.nextLine();
        System.out.println("请输入介绍信息");
        String info = scanner.nextLine();
        System.out.println("请输入价格");
        String price = scanner.nextLine();
        double p = Double.parseDouble(price);

        Book book = new Book(title,info,p);
        SqlUtil.doSqlWork(mapper -> {
            //mapper.addstudent(student);
            int i = mapper.addbook(book);
            if(i>0){
                System.out.println("书籍信息录入成功");
                log.info("新添加了一条书籍信息" + book);
            }else{
                System.out.println("书籍信息录入失败，请重试");
            }

        });
    }

    //添加学生信息
    private static void addStudent(Scanner scanner){
        //代码可以改进的地方 输入的内容不合法怎么办？
        //加入try catch
        System.out.println("请输入学生名字");
        String name = scanner.nextLine();
        System.out.println("请输入学生性别(男/女)");
        String sex = scanner.nextLine();
        System.out.println("请输入学生年纪");
        String grade = scanner.nextLine();
        int g = Integer.parseInt(grade);

        Student student = new Student(name,sex,g);
        SqlUtil.doSqlWork(mapper -> {
            //mapper.addstudent(student);
            int i = mapper.addstudent(student);
            if(i>0){
                System.out.println("学生信息录入成功");
                log.info("新添加了一条学生信息" + student);
            }else{
                System.out.println("学生信息录入失败，请重试");
            }

        });
    }
}
