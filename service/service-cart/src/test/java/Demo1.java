import java.math.BigDecimal;
import java.util.ArrayList;

public class Demo1 {

    public static void main(String[] args) {
        ArrayList<User> list = new ArrayList<>();
        User u1 = new User();
        u1.setNum(new BigDecimal(100));

        User u2 = new User();
        u2.setNum(new BigDecimal(200));

        list.add(u1);
        list.add(u2);

        BigDecimal result = list.stream().map(User::getNum).reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println(result);     //300
    }


}


class User {
    private BigDecimal num;

    public BigDecimal getNum() {
        return num;
    }

    public void setNum(BigDecimal num) {
        this.num = num;
    }
}
