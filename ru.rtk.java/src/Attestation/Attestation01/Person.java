package Attestation.Attestation01;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Person {
    private String name;
    private int money;
    private List<Product> productsInBucket = new ArrayList<>();

    public Person(String name, int money, List<Product> products) {
        if (money < 0) System.out.println("Деньги не могут быть отрицательным числом!");
        if (!checkName(name)) return;

        this.name = name;
        this.money = money;
        this.productsInBucket = products;
    }

    public Person(String name, int money) {
        if (money < 0) System.out.println("Деньги не могут быть отрицательным числом!");
        if (!checkName(name)) return;

        this.name = name;
        this.money = money;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (!checkName(name)) return;
        this.name = name;
    }

    private static boolean checkName(String name) {
        if (name == null || name.isBlank()){
            System.out.println("Имя не может быть пустой строкой!");
            return false;
        }
        if (name.length() < 3){
            System.out.println("Имя не может быть короче 3 символов!");
            return false;
        }
        return true;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        if (money < 0)
        {
            System.out.println("Деньги не могут быть отрицательным числом!");
            return;
        }
        this.money = money;
    }

    public List<Product> getProducts() {
        return productsInBucket;
    }

    public void setProducts(List<Product> products) {
        this.productsInBucket = products;
    }

    public boolean buyProduct(Product product){
        if (this.money >= product.getPrice())
        {
            this.money -= product.getPrice();
            this.productsInBucket.add(product);
            System.out.printf("%s купил %s.%n", this.name, product.getName());
            return true;
        }
        else {
            System.out.printf("%s не может позволить себе %s.%n", this.name, product.getName());
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return money == person.money && Objects.equals(name, person.name) && Objects.equals(productsInBucket, person.productsInBucket);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, money, productsInBucket);
    }

    @Override
    public String toString() {
        String productsInBucketPresentation = productsInBucket.stream().map(Product::toString).collect(Collectors.joining(", "));
        return productsInBucket.isEmpty()? this.name + " - " + "Ничего не куплено" : this.name + " - " + productsInBucketPresentation;
    }
}
