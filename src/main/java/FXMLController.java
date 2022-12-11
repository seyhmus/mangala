import java.net.URL;
import java.util.*;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.text.Font;

public class FXMLController implements Initializable {

    @FXML
    private Label label1;

    @FXML
    private Label label2;

    @FXML
    private Label bottomlabel;

    @FXML
    private Button button11;

    @FXML
    private Button button12;

    @FXML
    private Button button13;

    @FXML
    private Button button14;

    @FXML
    private Button button15;

    @FXML
    private Button button16;

    @FXML
    private Button button21;

    @FXML
    private Button button22;

    @FXML
    private Button button23;

    @FXML
    private Button button24;

    @FXML
    private Button button25;

    @FXML
    private Button button26;

    List<Labeled> controls = new ArrayList<>();
    Map<Labeled, Integer> controlMap = new HashMap<>();
    int user, label1index, label2index;

    @FXML
    private void handleButtonAction(ActionEvent event) {
        Button button = (Button) event.getSource();
        int index = controlMap.get(button);

        if((user == 0 && index > label1index) || (user == 1 && index < label1index)) {
            // other user's turn
            print();
            return;
        }

        int count = Integer.parseInt(button.getText());

        if(count == 0) {
            return;
        } else if(count == 1) {
            setValue(index,0);
            count++;
        } else {
            setValue(index,1);
        }

        int nextindex = index;
        int nextcount = -1;
        while(--count > 0) {
            nextindex++;
            nextindex %= controls.size();

            if((index<label1index && nextindex == label2index) || (index>label1index && nextindex == label1index)) {
                //bypass label2 for player 1
                //bypass label1 for player 2
                nextindex++;
                nextindex %= controls.size();
            }

            nextcount = getValue(nextindex) + 1;
            setValue(nextindex, nextcount);
        }

        if(checkComplete()) {
            return;
        }

        if(nextindex == label1index || nextindex == label2index) {
            // player continues
        } else {
            if (user == 0) {
                // player 1
                if (nextindex > label1index) {
                    if (nextcount % 2 == 0) {
                        // add to label1
                        setValue(nextindex, 0);
                        addTo(label1, nextcount);
                    }
                } else if (nextindex < label1index) {
                    if (nextcount == 1) {
                        // take it and the other
                        int otherindex = nextindex + 2 * (label1index - nextindex);
                        int total = pickAll(otherindex) + 1;
                        setValue(nextindex, 0);
                        addTo(label1, total);
                    }
                } else {
                    // player 1 continues
                    throw new RuntimeException("this line should not be reached");
                }
            } else {
                // player 2
                if (nextindex < label1index) {
                    if (nextcount % 2 == 0) {
                        //add to label2
                        setValue(nextindex, 0);
                        addTo(label2, nextcount);
                    }
                } else if (nextindex > label1index) {
                    if (nextcount == 1) {
                        // take it and the other
                        int otherindex = nextindex + 2 * (label1index - nextindex);
                        int total = pickAll(otherindex) + 1;
                        setValue(nextindex, 0);
                        addTo(label2, total);
                    }
                } else {
                    // player 2 continues
                    throw new RuntimeException("this line should not be reached");
                }
            }

            if(checkComplete()) {
                return;
            }
            changeTurn();
        }

        print();

    }

    private void changeTurn() {
        user++;
        setUser(user);
    }

    private void setUser(int value) {
        user = value%2;

        for(int i=0;i<=label1index;i++) {
            controls.get(i).setDisable(getValue(i) == 0 || user != 0);
        }
        for(int i=label1index+1;i<=label2index;i++) {
            controls.get(i).setDisable(getValue(i) == 0 || user == 0);
        }

    }

    private boolean isButton(int index) {
        return isButton(controls.get(index));
    }

    private boolean isButton(Object control) {
        return control.getClass() == button11.getClass();
    }

    private void setValue(int index, int val) {
        setValue(controls.get(index), val);
    }

    private void setValue(Labeled control, int val) {
        String text = String.valueOf(val);
        control.setText(text);
        control.setDisable(val == 0);
    }

    private boolean checkComplete() {
        if(user == 0) {
            // if all pits are 0, then collect the remaining and end the game
            int total = 0;
            for(int i=0;i<label1index;i++) {
                total += getValue(i);
            }
            if(total == 0) {
                // player 1 wins
                for(int i=label1index+1; i<label2index; i++) {
                    total += pickAll(i);
                }
                addTo(label1, total);
                bottomlabel.setText(String.format("player %d wins!", getValue(label1index) >= getValue(label2index) ? 1 : 2)); //final move owner wins if there is a draw
                return true;
            }
        } else {
            // if all pits are 0, then collect the remaining and end the game
            int total = 0;
            for(int i=label1index+1;i<label2index;i++) {
                total += getValue(i);
            }
            if(total == 0) {
                // player 1 wins
                for (int i = 0; i < label1index; i++) {
                    total += pickAll(i);
                }
                addTo(label2, total);
                bottomlabel.setText(String.format("player %d wins!", getValue(label2index) >= getValue(label1index) ? 2 : 1)); //final move owner wins if there is a draw
                return true;
            }
        }
        return false;
    }

    private int pickAll(int index) {
        int val = getValue(index);
        setValue(index,0);
        return val;
    }

    private void addTo(Labeled control, int val) {
        setValue(control, getValue(control)+val);
    }

    private int getValue(int index) {
        return getValue(controls.get(index));
    }

    private int getValue(Labeled control) {
        String text = control.getText();
        return Integer.parseInt(text);
    }

    private void print() {
        bottomlabel.setText(String.format("%s player %d's turn %s", user==0 ? "<--" : "", user+1, user==1 ? "-->" : ""));
        System.out.println(String.format("player %d's turn", user+1));

        int i = 0;
        for(Labeled control : controls) {
            System.out.print(getValue(control) + " ");
            if(i++ == label1index) {
                System.out.println();
            }
        }
        System.out.println();
        System.out.println();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Labeled controls[] = {button11, button12, button13, button14, button15, button16, label1, button21, button22, button23, button24, button25, button26, label2};

        int i = 0;
        for(Labeled control : controls) {
            control.setFont(Font.font(48));
            controlMap.put(control, i++);
            this.controls.add(control);
        }

        label1index = controlMap.get(label1);
        label2index = controlMap.get(label2);

        bottomlabel.setStyle("-fx-border-color:red; -fx-background-color: white;");

        Random random = new Random();
        if(random.nextBoolean()) {
            setUser(0);
        } else {
            setUser(1);
        }

        print();

    }    
}
