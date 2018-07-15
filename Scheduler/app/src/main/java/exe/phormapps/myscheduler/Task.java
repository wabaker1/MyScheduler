package exe.phormapps.myscheduler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by bakerwyatt19 on 5/18/2018.
 */


/**
 * Class Task
 * Description: Template for a task that a user will be able to create
 */
public class Task implements Serializable {

    private Calendar taskDate = Calendar.getInstance();
    private String taskName;
    private String taskDesc;
    private ArrayList<Integer> dailyRepeat = new ArrayList<>();
    private boolean reminder;
    private ArrayList<Integer> requestCode = new ArrayList<>();

    /**
     * Contructor for setting up task without customized daily or monthly repeating feature
     * @param date
     * @param name
     * @param desc
     */
    public Task(Calendar date, String name, String desc, ArrayList<Integer> days, boolean remind){
        taskDate = date;
        taskName = name;
        taskDesc = desc;
        for(int i = 0; i < days.size(); i++) {
            dailyRepeat.add(days.get(i));
        }
        reminder = remind;
    }

    /**
     * Returns the date of the task obj
     *
     * @return
     */
    public Calendar getTaskDate() {

        return taskDate;
    }

    /**
     * Sets the date of the task obj
     *
     * @param taskDate
     */
    public void setTaskDate(Calendar taskDate) {

        this.taskDate = taskDate;
    }

    /**
     * Returns the name of the task obj
     *
     * @return
     */
    public String getTaskName() {

        return taskName;
    }

    /**
     * Sets the name of the task obj
     *
     * @param taskName
     */
    public void setTaskName(String taskName) {

        this.taskName = taskName;
    }

    /**
     * sets request code  of the task obj
     *
     * @param reqCode
     */
    public void addRequestCode(int reqCode) {
        requestCode.add(reqCode);
    }

    /**
     * Returns the request code of the task obj
     *
     * @return
     */
    public ArrayList<Integer> getRequestCode() {
        return requestCode;
    }

    /**
     * Returns the description of the task obj
     * @return
     */
    public String getTaskDesc() {

        return taskDesc;
    }

    /**
     * sets the description of the task obj
     *
     * @param taskDesc
     */
    public void setTaskDesc(String taskDesc) {
        this.taskDesc = taskDesc;
    }

    /**
     * Returns the repeating days of the task obj
     *
     * @return
     */
    public ArrayList<Integer> getDailyRepeat() {

        return dailyRepeat;
    }

    /**
     * Sets the repeatable days of the task obj
     * @param dailyRepeat
     */
    public void setDailyRepeat(ArrayList<Integer> dailyRepeat) {

        this.dailyRepeat = dailyRepeat;
    }

    /**
     * Returns if the task will remind
     *
     * @return
     */
    public boolean isReminder() {

        return reminder;
    }

    /**
     * Sets if the task will remind
     *
     * @param reminder
     */
    public void setReminder(boolean reminder) {

        this.reminder = reminder;
    }
}
