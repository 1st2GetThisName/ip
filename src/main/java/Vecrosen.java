import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Vecrosen {

    private enum TaskType {todo, deadline, event};
    private static ArrayList<Task> list;

    /**
     * Outputs a string with an indentation, to differentiate Vecrosen's dialog from the user's.
     * @param s The string to be printed.
     */
    private static void speak(String s) {
        System.out.print("    ");
        System.out.println(s);
    }

    /**
     * Saves the task list to vecrosen.txt.
     */
    private static void save() {
        try {
            File f = new File("data/vecrosen.txt");
            FileWriter fw = new FileWriter(f);
            for (int i = 0; i < list.size(); ++i) {
                Task t = list.get(i);
                if (i != 0) {
                    fw.write('\n');
                }
                fw.write(t.getDescription());
                fw.write("\n" + t.isDone());
                if (t.getClass() == Task.class) {
                    fw.write(" " + TaskType.todo.ordinal());
                } else if (t.getClass() == Deadline.class) {
                    fw.write(" " + TaskType.deadline.ordinal() + '\n');
                    Deadline d = (Deadline) t;
                    fw.write(d.getBy());
                } else if (t.getClass() == Event.class) {
                    fw.write(" " + TaskType.event.ordinal() + '\n');
                    Event e = (Event) t;
                    fw.write(e.getBegin() + '\n');
                    fw.write(e.getEnd());
                } else {
                    System.err.println("Unrecognized task type when saving!");
                }
            }
            fw.close();
        } catch (IOException e) {
            System.err.println("Savefile cannot be accessed!");
        }
    }

    /**
     * Loads the data from the file into the list.
     * @param file
     */
    private static void load(File file) {
        if (!file.exists()) {
            return;
        }
        try {
            Scanner s = new Scanner(file);
            while (s.hasNextLine()) {
                String description = s.nextLine();
                boolean isDone = s.nextBoolean();
                int tth = s.nextInt();
                TaskType tt = TaskType.values()[tth];
                s.nextLine();
                switch (tt) {
                case event:
                    String begin = s.nextLine();
                    String end = s.nextLine();
                    list.add(new Event(description, begin, end));
                    break;
                case deadline:
                    String by = s.nextLine();
                    list.add(new Deadline(description, by));
                    break;
                case todo:
                    list.add(new Task(description));
                    break;
                }
                list.get(list.size() - 1).setDone(isDone);
            }
        } catch (FileNotFoundException e) {
            System.err.println("File exists but cannot be found...?");
        } catch (NoSuchElementException | IndexOutOfBoundsException e) {
            speak("Savefile is corrupted!");
            System.err.println(list.size());
            list.clear();
        }
    }

    public static void main(String[] args) {
        list = new ArrayList<Task>();
        speak("Hello, I'm Vecrosen.");
        speak("What can I do for you?");
        load(new File("data/vecrosen.txt"));
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input;
            input = scanner.nextLine();
            if (input.equals("bye")) {
                break;
            } else if (input.matches("mark \\d+")) {
                int itemNo = Integer.parseInt(input.substring(5));
                if (itemNo < 1 || itemNo > list.size()) speak("Invalid task number!");
                else {
                    list.get(itemNo-1).setDone(true);
                    speak("Task marked as complete: " + list.get(itemNo-1).getDescription());
                }
            } else if (input.matches("unmark \\d+")) {
                int itemNo = Integer.parseInt(input.substring(7));
                if (itemNo < 1 || itemNo > list.size()) speak("Invalid task number!");
                else {
                    list.get(itemNo-1).setDone(false);
                    speak("Task marked as incomplete: " + list.get(itemNo-1).getDescription());
                }
            } else if (input.equals("list")) {
                for (int i = 0; i < list.size(); ++i) {
                    speak((i+1) + "." + list.get(i).toString());
                }
            } else if (input.matches("todo .+")) {
                String desc = input.substring(5);
                list.add(new Task(desc));
                speak("Todo added: " + desc);
            } else if (input.matches("deadline .+ /by .+")) {
                int byStart = input.indexOf("/by ");
                String by = input.substring(byStart + 4);
                String desc = input.substring(9, byStart - 1);
                list.add(new Deadline(desc, by));
                speak("Deadline added: " + desc);
            } else if (input.matches("event .+ /begin .+ /end .+")) {
                int beginStart = input.indexOf("/begin ");
                int endStart = input.indexOf("/end ");
                String desc = input.substring(6, beginStart - 1);
                String begin = input.substring(beginStart + 7, endStart - 1);
                String end = input.substring(endStart + 5);
                list.add(new Event(desc, begin, end));
                speak("Event added: " + desc);
            } else if (input.matches("event .+ /end .+ /begin .+")) {
                int beginStart = input.indexOf("/begin ");
                int endStart = input.indexOf("/end ");
                String desc = input.substring(6, endStart - 1);
                String end = input.substring(endStart + 5, beginStart - 1);
                String begin = input.substring(beginStart + 7);
                list.add(new Event(desc, begin, end));
                speak("Event added: " + desc);
            } else if (input.matches("delete \\d+")) {
                int itemNo = Integer.parseInt(input.substring(7));
                if (itemNo < 1 || itemNo > list.size()) speak("Invalid task number!");
                else {
                    speak("Removing task: " + list.get(itemNo - 1).getDescription());
                    list.remove(itemNo - 1);
                    speak("You now have " + list.size() + " tasks left in record.");
                }
            } // TODO: make helper function
            else if (input.startsWith("todo")) {
                speak("Invalid format.");
                speak("Usage: todo [description]");
            } else if (input.startsWith("deadline")) {
                speak("Invalid format.");
                speak("Usage: deadline [description] /by [deadline]");
            } else if (input.startsWith("event")) {
                speak("Invalid format.");
                speak("Usage: event [description] /begin [startTime] /end [endTime]");
            } else if (input.startsWith("mark")) {
                speak("Invalid format.");
                speak("Usage: mark [taskID]");
            } else if (input.startsWith("unmark")) {
                speak("Invalid format.");
                speak("Usage: unmark [taskID]");
            } else if (input.startsWith("delete")) {
                speak("Invalid format.");
                speak("Usage: delete [taskID]");
            } else {
                speak("Sorry, I don't understand.");
                speak("Commands: todo deadline event mark unmark delete list bye");
            }
            save();
        }
        speak("Bye. Hope to see you again soon!");
    }
}
