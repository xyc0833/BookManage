package book.manage.mapper;

import book.manage.entity.Book;
import book.manage.entity.Student;
import org.apache.ibatis.annotations.Insert;

public interface BookMapper {

    @Insert("insert into student(name,sex,grade) values(#{name},#{sex},#{grade}) ")
    int addstudent(Student student);

    @Insert("insert into book(title,info,price) values(#{title},#{info},#{price}) ")
    int addbook(Book book);
}
