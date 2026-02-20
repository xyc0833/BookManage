package book.manage.mapper;

import book.manage.entity.Book;
import book.manage.entity.Borrow;
import book.manage.entity.Student;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface BookMapper {

    @Insert("insert into student(name,sex,grade) values(#{name},#{sex},#{grade}) ")
    int addstudent(Student student);

    @Insert("insert into book(title,info,price) values(#{title},#{info},#{price}) ")
    int addbook(Book book);

    @Insert("insert into borrow(sid,bid) values (#{sid},#{bid})")
    int addBorrow(@Param("sid") int sid, @Param("bid") int bid);

    //column：数据库表中的列名（比如表字段叫id）
    //property：Java 实体类中的属性名
    //id = true ：标记该字段是主键
    @Results({
            @Result(column = "id",property = "id",id = true),
            //xml中的association进行关联，形成多对一的关系
            //等加于注解中的one
            @Result(column = "sid",property = "student",one = @One(select = "getStudentBySid")),
            @Result(column = "bid",property = "book",one = @One(select = "getBookByBid"))
    })
    @Select("select * from borrow")
    List<Borrow>  getBorrowList();

    @Select("select * from student where sid = #{sid}")
    Student getStudentBySid(int sid);

    @Select("select * from book where bid = #{bid}")
    Book getBookByBid(int bid);

    @Select("select * from student")
    List<Student> getStudentList();

    @Select("select * from book")
    List<Book> getBookList();
}
