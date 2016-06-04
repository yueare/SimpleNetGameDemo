
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Vector;

/**
 * Created by FJU on 2015/5/21.
 */
public class gamePanel extends JPanel {

    int x=10;
    int y=10;
    int Direction=39;
    int ID;
    int score=0;
    static int step=8;
    Color color;
    int size;
    Image backimg;
    Image [] planes;
    Image [] players;
    Vector<Points> obj = new Vector<Points>();
    Vector<Points> obstacleObj = new Vector<Points>();
    gamePanel() throws IOException {
        this.setBackground(Color.white);
        this.setDoubleBuffered(true);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        backimg = toolkit.getImage("D:/JAVA/connection/src/back.jpg");

        planes = new Image[5];
        for(int i=0;i<planes.length;i++)
        {
            planes[i] = toolkit.getImage("D:/JAVA/connection/src/plane"+i+".png");
        }

        players = new Image[5];
        for(int i=0;i<players.length;i++)
        {
            players[i] = toolkit.getImage("D:/JAVA/connection/src/plane"+i+i+".png");
        }
    }



    public void paint(Graphics g) {
        // 在重绘函数中实现双缓冲机制
        BufferedImage bi = (BufferedImage)this.createImage(getWidth(),getHeight());
        Graphics g2 = bi.createGraphics();
        try
        {


            g2.drawImage(backimg,0,0,this);

//            for(int i=0;i<obstacleObj.size();i++)
//            {
//                g2.setColor(obstacleObj.get(i).getColor());
//                g2.fillRect(obstacleObj.get(i).x, obstacleObj.get(i).y, obstacleObj.get(i).getSize(), obstacleObj.get(i).getSize());
//            }
            for(int i=0;i<obstacleObj.size();i++)//载入图片资源
            {
                int pos=0;
                if(obstacleObj.get(i).getColor().equals(Color.cyan))
                {
                    pos=0;
                }
                else if(obstacleObj.get(i).getColor().equals(Color.green))
                {
                    pos=1;
                }
                else if(obstacleObj.get(i).getColor().equals(Color.blue))
                {
                    pos=2;
                }
                else if(obstacleObj.get(i).getColor().equals(Color.red))
                {
                    pos=3;
                }
                else if(obstacleObj.get(i).getColor().equals(Color.orange))
                {
                    pos=4;
                }
                g2.drawImage(planes[pos],obstacleObj.get(i).x,obstacleObj.get(i).y,40,40,this);
            }


            for(int i =0;i<obj.size();i++)
            {
                int pos=0;
                if(obj.get(i).getColor().equals(Color.cyan))
                {
                    pos=0;
                }
                else if(obj.get(i).getColor().equals(Color.green))
                {
                    pos=1;
                }
                else if(obj.get(i).getColor().equals(Color.blue))
                {
                    pos=2;
                }
                else if(obj.get(i).getColor().equals(Color.red))
                {
                    pos=3;
                }
                else if(obj.get(i).getColor().equals(Color.orange))
                {
                    pos=4;
                }
//                g2.setColor(obj.get(i).getColor());
//                g2.fillRect(obj.get(i).x, obj.get(i).y, obj.get(i).getSize(), obj.get(i).getSize());
                g2.drawImage(players[pos],obj.get(i).x, obj.get(i).y, obj.get(i).getSize()+30, obj.get(i).getSize()+30,this);
                if(obj.get(i).ID==this.ID)
                    if(obj.get(i).getSize()==80)
                    {
                        g2.setColor(Color.white);
                        g2.drawString("机毁人亡",10,21);
                    }
                    else
                    {
                        g2.setColor(Color.white);
                        g2.drawString("营救的飞机数目:"+score,40+80, 21);
                        g2.drawString("生命:", 10, 21);
                        g2.drawRect(40, 12, 80 - 10 + 3, 10);
                        g2.setColor(Color.red);
                        g2.fillRect(40 + 2, 15, (80 - obj.get(i).size), 6);

                    }

            }
            g.drawImage(bi, 0, 0, this);
        }
        catch (Exception eee)
        {
            return;
        }



    }


    public  void move(int keyCode)//keycode 37 38 39 40 L T R B
    {
        if(isOver())
        {
            return;
        }
        this.Direction =keyCode;
        if (this.Direction==37)
        {
            if(x>0)
                x-=step;

        }
        if (this.Direction==38)
        {
            if(y>0)
                y-=step;
        }
        if (this.Direction==39)
        {
            if(x<790-size)
                x+=step;
        }
        if (this.Direction==40)
        {
            if(y<545-size)
                y+=step;
        }
        for(int i=0;i<obj.size();i++)//队列数据同步到本地
        {
            if(this.ID==obj.get(i).ID)
            {
                obj.get(i).x=this.x;
                obj.get(i).y=this.y;
                obj.get(i).r=color.getRed();
                obj.get(i).g= color.getGreen();
                obj.get(i).b=color.getBlue();
                obj.get(i).size=size;
            }
        }

    }

    public void processMsg(String msg)//处理服务器返回数据
    {

        if(Integer.valueOf(msg.substring(0,2))==-1)//检查第一次ID 如果是障碍物标记把处理权交给processobstacleObj
        {
            processobstacleObj(msg);
            return;
        }

        String tmp1[] = msg.split(":");
        String tmp2[][] = new String[tmp1.length][];
        obj.clear();
        for(int i=0;i<tmp1.length;i++)
        {
            tmp2[i]=tmp1[i].split(",");
            obj.add(new Points(Integer.valueOf(tmp2[i][0]), Integer.valueOf(tmp2[i][1]), Integer.valueOf(tmp2[i][2]),Integer.valueOf(tmp2[i][3]),Integer.valueOf(tmp2[i][4]),Integer.valueOf(tmp2[i][5]),Integer.valueOf(tmp2[i][6]),Integer.valueOf(tmp2[i][7])));//保存队列
            if(obj.get(i).ID==this.ID)//刷新当前坐标
            {
                this.x = obj.get(i).x;
                this.y = obj.get(i).y;
                this.color=obj.get(i).getColor();
                this.size=obj.get(i).getSize();
                this.score=obj.get(i).score;
            }
        }

    }

    public void processobstacleObj(String msg)//处理服务器返回数据
    {
        String tmp1[] = msg.split(":");
        String tmp2[][] = new String[tmp1.length][];
        obstacleObj.clear();
        for(int i=0;i<tmp1.length;i++)
        {
            tmp2[i]=tmp1[i].split(",");
            obstacleObj.add(new Points(Integer.valueOf(tmp2[i][0]), Integer.valueOf(tmp2[i][1]), Integer.valueOf(tmp2[i][2]),Integer.valueOf(tmp2[i][3]),Integer.valueOf(tmp2[i][4]),Integer.valueOf(tmp2[i][5]),Integer.valueOf(tmp2[i][6]),Integer.valueOf(tmp2[i][7])));//保存队列
        }

    }

    public String getData()//数据封装
    {
        String msg=""+ ID+","+x+","+y+","+color.getRed()+","+color.getGreen()+","+color.getBlue()+","+size+","+score;
        return msg;
    }

    public  boolean isOver()
    {
        if(size>=80)
        {
            return true;
        }

        return false;
    }


}
