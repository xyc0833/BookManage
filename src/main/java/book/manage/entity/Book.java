package book.manage.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Book {
    int bid;
    String title;
    String info;
    double price;

    public Book(String title, String info, double price) {
        this.title = title;
        this.info = info;
        this.price = price;
    }
}
