import java.awt.*;

/**
 * Created by FJU on 2015/5/20.
 */

//�������л� ��
public class Points {

    //���
    public int ID;
    //����
    int x,y;
    //��С
    int size;
    //��ɫ����
    int r,g,b,a;
    //����
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
