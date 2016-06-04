import java.awt.*;

/**
 * Created by FJU on 2015/5/20.
 */

//数据序列化 类
public class Points {

    //身份
    public int ID;
    //坐标
    int x,y;
    //大小
    int size;
    //颜色分量
    int r,g,b,a;
    //分数
    int score=0;

    Points(int ID,int x,int y,int r,int g, int b,int size,int score)
    {

        this.ID=ID;
        this.x=x;
        this.y=y;
        this.r=r;
        this.g=g;
        this.b=b;
        this.a=255;
        this.size=size;
        this.score=score;
    }

    public  Color getColor()
    {
        return new Color(r,g,b,a);
    }
    public  int getSize()
    {
        return size;
    }

}
