package Homeworks.Homework06.Shop;


import java.util.Objects;

public class Product {
    private String name;
    private int price;

    public Product(String name, int price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()){
            System.out.println("Название продукта не может быть пустой строкой!");
            return;
        }

        this.name = name;
    }

    public void setPrice(int price) {
        if (price < 0)
        {
            System.out.println("Стоимость продукта не может быть отрицательным числом!");
            return;
        }

        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return price == product.price && Objects.equals(name, product.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, price);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
