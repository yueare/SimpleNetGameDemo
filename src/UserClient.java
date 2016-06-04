/**
 * Created by FJU on 2015/5/20.
 * */


import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.*;


public class UserClient extends JFrame {
//  UI组件
    private JPanel p= new JPanel();
    private JLabel info = new JLabel();
    private JButton jbtn = new JButton("再来一次");
//  流
    private DataOutputStream toserver ;
    private DataInputStream fromserver;

    private int ID;
//  游戏画板
    private gamePanel gp;

    private Socket client;

    public UserClient() throws IOException {
        //布局设定
        JButton jp = new JButton("  ");
        p.setLayout(null);
        this.setTitle("Client");
        this.add(p);
        p.add(jp);
        gp = new gamePanel();
        gp.setBounds(0, 0, 800, 550);
        info.setBounds(0, 552, 600, 20);
        jbtn.setBounds(600, 552, 150, 20);
        jbtn.setVisible(false);
        p.add(gp);p.add(info);p.add(jbtn);

        info.setText("信息");
        this.setSize(800, 602);
        this.setResizable(false);
        setLocationRelativeTo(null);
        //退出请求 监听
        this.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent we) {
                        try {
                            if (toserver != null)
                            {
                                //发送退出请求
                                toserver.writeUTF(ID + ",exit");
                            }

                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
        );
        //重新开始请求命令发送 监听
        jbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    //发送重新开始请求
                    String str= gp.getData()+",reset";
                    toserver.writeUTF(str);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });


        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        //按键监听
        gp.addKeyListener(new keyListener());
        jp.addKeyListener(new keyListener());


        try {
            //连接服务器
            client = new Socket("127.0.0.1",54321);
            //client.setTcpNoDelay(true);
            //初始化身份ID 和 数据流
            ID=client.getLocalPort();
            gp.ID=ID;
            toserver = new DataOutputStream(client.getOutputStream());
            fromserver = new DataInputStream(client.getInputStream());
            //发送游戏连接请求
            toserver.writeUTF(ID + ",connected");
            this.setTitle("穿越飞行 Client:" + ID);
            toserver.flush();




            //取服务器反馈信息
            while(true){
                    String msg =fromserver.readUTF();
                    if(msg!=null)
                    {
                        gp.processMsg(msg);//处理服务器返回的数据包
                        if(gp.isOver())//本地死亡检测
                        {
                            info.setText("机毁人亡");
                            jbtn.setVisible(true);
                        }
                        else
                        {
                            //提示信息
                            info.setText("键盘上下左右控制移动  坐标：x=" + gp.x + " y=" + gp.y + " 剩余生命：" + (80 - gp.size));
                            jbtn.setVisible(false);

                        }
                        gp.repaint();//刷新

                    }
                    //System.out.println(msg);

            }
        } catch (UnknownHostException e) {
        // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
        // TODO Auto-generated catch block
            e.printStackTrace();
            setTitle("连接服务器失败");
        }



    }

    //按键监听类
    private class  keyListener extends KeyAdapter{
        public void keyPressed(KeyEvent e)
        {
            String str="";
            if(e.getKeyCode() ==KeyEvent.VK_UP||e.getKeyCode() ==KeyEvent.VK_DOWN
                    ||e.getKeyCode() ==KeyEvent.VK_LEFT||e.getKeyCode() ==KeyEvent.VK_RIGHT)
            {
                //------------------------同步状态

                gp.move(e.getKeyCode());

                str = gp.getData();//获取当前身份的数据包
                try {
                    if(!gp.isOver()){
                        toserver.writeUTF(str);//发送状态,请求服务器移动当前控制单位
                        toserver.flush();
                    }

                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
        }

    }



    public static void main(String[] args) throws IOException {
        new clientForm();


    }
}

//启动登陆界面
class clientForm  extends JFrame
{
    clientForm()
    {
        //布局
        setLayout(null);
        JButton startBt = new JButton("登陆");
        JButton exitBt = new JButton("退出");
        JTextArea jLabel = new JTextArea();
        startBt.setBounds(200 - 2, 200, 100, 40);
        exitBt.setBounds(200 - 2, 245, 100, 40);
        jLabel.setBounds(100,50,300,50);
        jLabel.setText("超级简单的多人在线游戏，你要躲避异类飞机，吃掉同类飞机,\n活着就好。你也可以和玩家交换飞机体积");
        jLabel.setEnabled(false);
        this.add(startBt);
        this.add(exitBt);
        this.add(jLabel);
        this.setTitle("穿越飞行营救");

        startBt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            dispose();
                            //启动线程登录游戏窗体
                            new UserClient();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }).start();

            }
        });
        exitBt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        setSize(500, 400);
        setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setVisible(true);
    }
}
