package ui;

import utils.RgbColor;
import utils.algebra.Vec2;
import utils.io.DataExporter;

import javax.swing.*;

import raytracer.RenderMode;
import raytracer.Interface.IKeyListener;
import scene.Scene;
import scene.SceneObject;
import scene.light.Light;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Window implements KeyListener{

    public int GetWidth() {
        return mWidth;
    }

    public int GetHeight() {
        return mHeight;
    }


    private int mWidth;
    private int mHeight;

    private BufferedImage mBufferedImage;

    private JFrame mFrame;

    /**
     Create render window with the given dimensions
     **/
    public Window(int width, int height){
        mWidth = width;
        mHeight = height;

        // we are using only one frame
        mBufferedImage = new BufferedImage(mWidth, mHeight, BufferedImage.TYPE_INT_RGB);

        createFrame();
        setupBar();
       
    }

    public BufferedImage getBufferedImage(){
        return mBufferedImage;
    }

    /**
     Setup render frame with given parameters
     **/
    private void createFrame(){
        JFrame frame = new JFrame();

        frame.getContentPane().add(new JLabel(new ImageIcon(mBufferedImage)));
        frame.setSize(mBufferedImage.getHeight() + frame.getSize().height, mBufferedImage.getWidth() + frame.getSize().width);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);

        

        mFrame = frame;
        mFrame.addKeyListener(this);
        
    }

    /**
     Draw debug information
     **/
    private void setOutputLabel(String text, int recursions, int antiAliasing){
        Graphics graphic = mBufferedImage.getGraphics();
        graphic.setColor(Color.black);
        graphic.fill3DRect(0,mHeight - 30,mWidth,mHeight,true);
        graphic.setColor(Color.green);
        graphic.drawString("Elapsed rendering time: " + text + " sec, Recursions: " + recursions + ", AA: x" + antiAliasing, 10, mHeight - 10);
        //RedrawLabel();
        mFrame.repaint();
    }

    /**
     Draw pixel to our render frame
     **/
    public void setPixel(BufferedImage bufferedImage, RgbColor color, Vec2 screenPosition){
        bufferedImage.setRGB((int)screenPosition.x, (int)screenPosition.y, color.getRGB());
        mFrame.repaint();
    }

    /**
     Export the rendering to an PNG image with rendering information
     **/
    public void exportRendering(String text, int recursions, int antiAliasing, boolean showLabel){

        if(showLabel) {
            setOutputLabel(text, recursions, antiAliasing);
        }
        DataExporter.exportImageToPng(mBufferedImage, "raytracing.png");
    }





    // ----------- KEY LISTENING -----------
    private ArrayList<IKeyListener> registered = new ArrayList<>();

    public void registerKeyListener(IKeyListener listener) {
        if (listener != null) {
            registered.add(listener);
        }
    }



    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'keyTyped'");
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'keyPressed'");
        
        /*
        KeyEvent.VK_W
        KeyEvent.VK_A
        KeyEvent.VK_S
        KeyEvent.VK_D
        
        KeyEvent.VK_SPACE
        KeyEvent.VK_SHIFT
        KeyEvent.VK_ESCAPE
        
        KeyEvent.VK_LEFT
        KeyEvent.VK_RIGHT
        KeyEvent.VK_UP
        KeyEvent.VK_DOWN
        
        
        
        
        KeyEvent.VK_1
        KeyEvent.VK_2
        KeyEvent.VK_3
        KeyEvent.VK_4
        KeyEvent.VK_5
        KeyEvent.VK_6
        KeyEvent.VK_7
        KeyEvent.VK_8
        KeyEvent.VK_9
        KeyEvent.VK_0
        
        
        */
    
    
    }

    @Override
    public void keyReleased(KeyEvent e) {
        System.out.println("KEY RELEASED " + e);
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'keyReleased'");
        if (IKeyListener.isNumber(e)) {
            System.out.println("KEY RELEASED NUMBER" + e);
            for (int i = 0; i < registered.size(); i++) {
                IKeyListener current = registered.get(i);
                if (current != null) {
                    current.ReceiveKey(e);
                }
            }
        }
    
    
    
    }


    
   

    public void setRenderMode(RenderMode mode) {
        for (int i = 0; i < registered.size(); i++) {
            IKeyListener current = registered.get(i);
            if (current != null) {
                current.UpdateRenderMode(mode);
            }
        }

    }
    
    public void reloadScene() {
        for (int i = 0; i < registered.size(); i++) {
            IKeyListener current = registered.get(i);
            if (current != null) {
                current.Reload();
            }
        }
        
    }




    /// new
    private JPanel container;
    private JPanel modePanel;
    private JPanel sceneTreePanel;
    private JPanel presetScenePanel;
    private ButtonGroup modeGroup;
   

    private Color panelColor = Color.WHITE;


    private JPanel defaultPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(panelColor);
        panel.setOpaque(true);
        return panel;
    }

    private JPanel defaultPanel(int axis) {
        JPanel panel = defaultPanel();
        panel.setLayout(new BoxLayout(panel, axis));
        return panel;
    }

    private JPanel makePanelExpandable(int axis, String name) {
        JCheckBox toggle = new JCheckBox(name);
        toggle.setSelected(true);

        JPanel panel = defaultPanel(axis);

        panel.setVisible(true); // initial zu
        toggle.addActionListener(e -> {
            panel.setVisible(toggle.isSelected());
            panel.getParent().revalidate();
            panel.getParent().repaint();
        });
        container.add(toggle);
        return panel;
    }



    private void setupBar() {
        container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        
        
        
        //modePanel = new JPanel();
        //modePanel.setLayout(new BoxLayout(modePanel, BoxLayout.X_AXIS));
        modePanel = makePanelExpandable(BoxLayout.Y_AXIS, "Render Modes");
        
        //sceneTreePanel.setLayout(new BoxLayout(sceneTreePanel, BoxLayout.Y_AXIS));

        modeGroup = new ButtonGroup();
        modePanel.add(new JLabel("RenderModes: "));
        addRenderModeButton("Gradient ", RenderMode.RenderModePraikum0_0_colorGradient);
        addRenderModeButton("Gradient 2", RenderMode.RenderModePraikum0_1_colorCircle);
        addRenderModeButton("Camera Colors", RenderMode.RenderModePraikum1_0_cameraColors);
        addRenderModeButton("Intersect Draw", RenderMode.RenderModePraikum1_2_SpehereIntersect);
        addRenderModeButton("Lambert Bounce 1", RenderMode.RenderModePraikum2_3_LambertBounceOne);
        addRenderModeButton("Phong Bounce 1", RenderMode.RenderModePraikum2_4_PhongBounceOne);
        addRenderModeButton("Phong Bounce N", RenderMode.RenderModePraikum3_5_PhongBounceN);
        addRenderModeButton("Phong Bounce N Refract", RenderMode.RenderModePraikum3_6_PhongBounceNRefract);
        container.add(modePanel);


        


        
        SetupPresetPanel();
        modePanel.add(presetScenePanel);
        

        mFrame.add(container, BorderLayout.EAST);

        PackRebuildFrame();
    }
    
    private void SetupPresetPanel() {
        presetScenePanel = new JPanel();
        presetScenePanel.setLayout(new BoxLayout(presetScenePanel, BoxLayout.Y_AXIS));
    }



    private void addRenderModeButton(String text, RenderMode mode) {

        JRadioButton button = new JRadioButton(text);
        button.setFocusable(false); //key inputs nicht blockieren

        button.addActionListener(e -> {

            System.out.println("Selected: " + mode);

            setRenderMode(mode);

            // Hier kannst du dein Rendering ändern

        });
        modeGroup.add(button);
        modePanel.add(button);
        PackRebuildFrame();
    }


    
    public void SetupSceneTreeUi(Scene scene) {
        sceneTreePanel = makePanelExpandable(BoxLayout.Y_AXIS, "Scene Tree");
        ArrayList<SceneObject> list = scene.GetSceneObjects();

        //scene object menu
        sceneTreePanel.add(new JLabel("Scene:"));
        for (SceneObject s : list) {
            sceneTreePanel.add(MakeFromSceneObject(s));
        }

        //light object menu
        ArrayList<Light> lightsList = scene.GetLights();
        sceneTreePanel.add(new JLabel("Lights:"));
        for (SceneObject s : lightsList) {
            JCheckBox box = MakeSceneObjectCheckBox(s);
            sceneTreePanel.add(box);
        }
        container.add(sceneTreePanel);

        setupDebugOptions(scene);
        SetupRenderBouncesSlider(scene);
        
        PackRebuildFrame();
    }


    JComponent MakeFromSceneObject(SceneObject s) {
        java.util.List<SceneObject> childs = s.GetChilds();
        
        
        if (childs.size() > 0) {
            JPanel base = defaultPanel(BoxLayout.Y_AXIS);
            
            base.add(MakeSceneObjectCheckBox(s));
            base.add(MakeSceneObjectSubMenu(childs));
            return base;
        }
        return MakeSceneObjectCheckBox(s);
    }




    JCheckBox MakeSceneObjectCheckBox(SceneObject s) {
        String name = s.name;
        JCheckBox box = new JCheckBox(name);
        box.setFocusable(false);
        box.addActionListener(e -> {
            s.IsEnabled = !s.IsEnabled;
            //reloadScene();
        });
        box.setSelected(s.IsEnabled);
        return box;
    }

    

    JPanel MakeSceneObjectSubMenu(java.util.List<SceneObject> objects) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 5));
        //panel.add(new JLabel("Child Objects"));
        for (SceneObject s : objects) {
            JCheckBox box = new JCheckBox(s.name);
            box.setSelected(s.IsEnabled);
            box.setFocusable(false);
            box.addActionListener(e -> {
                s.IsEnabled = box.isSelected();
                //reloadScene();
            });
            panel.add(box);
        }
        return panel;
    }


    
    private void PackRebuildFrame() {
        modePanel.revalidate();
        modePanel.repaint();
        mFrame.pack();
    }


    private void setupDebugOptions(Scene scene) {
        sceneTreePanel.add(new JLabel("Debug Options:"));
        
        JCheckBox box1 = new JCheckBox("Debug Lights");
        box1.setFocusable(false);
        box1.addActionListener(e -> {
            scene.EnableDebugLights(box1.isSelected());
            //reloadScene();
        });
        box1.setSelected(false);
        sceneTreePanel.add(box1);


        JCheckBox box2 = new JCheckBox("Debug Refraction");
        box2.setFocusable(false);
        box2.addActionListener(e -> {
            scene.EnableDebugRefract(box2.isSelected());
            //reloadScene();
        });
        box2.setSelected(false);
        sceneTreePanel.add(box2);


        

        PackRebuildFrame();
        
    }


    private void SetupRenderBouncesSlider(Scene scene) {
        JSlider slider = new JSlider(1, 5, 1);

        slider.setFocusable(false);
        slider.setMajorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);

        slider.addChangeListener(e -> {
            int value = slider.getValue();
            scene.setBounces(value);
            System.out.println("Set Bounces: " + value);
            
        });

        sceneTreePanel.add(slider);

        JButton button = new JButton("Reload");
        button.addActionListener(e -> {
            reloadScene();
        });
        sceneTreePanel.add(button);



        PackRebuildFrame();
    }

    


}
