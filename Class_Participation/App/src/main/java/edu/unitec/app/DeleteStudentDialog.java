package edu.unitec.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Alberto on 03/06/2014.
 */
public class DeleteStudentDialog extends DialogFragment {
    StudentItemAdapter arrayAdapter;
    List<StudentItem> listViewStudentNameList;
    List<Integer> list_StudentID;
    ArrayList<Integer> SelectedIdxToDelete;
    Section currentSection;
    String elements[];

    DeleteStudentDialog(){}
    DeleteStudentDialog(Section currentSection, StudentItemAdapter arrayAdapter, List<StudentItem> list, List<Integer> list2)
    {
        this.currentSection = currentSection;
        this.arrayAdapter = arrayAdapter;
        this.listViewStudentNameList = list;
        this.SelectedIdxToDelete=new ArrayList<Integer>();
        this.list_StudentID=list2;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_deletestudent, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete Students");
        builder.setView(view);
        elements=new String[listViewStudentNameList.size()];
        for(int i=0;i<listViewStudentNameList.size();i++){
            elements[i]=listViewStudentNameList.get(i).getStudentName();
        }
        builder.setMultiChoiceItems(elements,null,new DialogInterface.OnMultiChoiceClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                     if (isChecked) {
                         SelectedIdxToDelete.add(which);
                     }else if(SelectedIdxToDelete.contains(which)){
                         SelectedIdxToDelete.remove(Integer.valueOf(which));
                     }
            }
        });
        builder.setNegativeButton("Close", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                DeleteStudentDialog.this.getDialog().cancel();
            }
        });

        builder.setPositiveButton("Delete",
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //Do nothing here because we override this button later to change the close behaviour.
                        //However, we still need this because on older versions of Android unless we
                        //pass a handler the button doesn't get instantiated
                    }
                });

        return builder.create();
    }

    @Override
    public void onStart(){
        super.onStart();    //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
        AlertDialog dialog = (AlertDialog)getDialog();

        if(dialog != null){
            Button positiveButton = (Button) dialog.getButton(Dialog.BUTTON_POSITIVE);

            positiveButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    ArrayList<Integer> StudentID_toErase = new ArrayList<Integer>();
                    Collections.sort(SelectedIdxToDelete);
                    for(int i=0; i<SelectedIdxToDelete.size(); i++){
                        StudentID_toErase.add(list_StudentID.get(SelectedIdxToDelete.get(i)));
                    }
                    try{
                        DatabaseHandler db = new DatabaseHandler(v.getContext());
                        db.deleteStudent(StudentID_toErase);
                        if(db.tableStudentIsEmpty(currentSection.get_SectionId())) {
                            //if table student is empty show the invisible menuItems
                            MenuItem item_statistics = ((StudentActivity) getActivity()).menu.findItem(R.id.item_statistics);
                            MenuItem item_student = ((StudentActivity) getActivity()).menu.findItem(R.id.item_Student);
                            MenuItem save_student = ((StudentActivity) getActivity()).menu.findItem(R.id.item_addStudent);
                            MenuItem save_students = ((StudentActivity) getActivity()).menu.findItem(R.id.save_students);
                            MenuItem delete_student = ((StudentActivity) getActivity()).menu.findItem(R.id.delete_students);
                            MenuItem newAssignment = ((StudentActivity) getActivity()).menu.findItem(R.id.item_newAssignment);
                            MenuItem newHomework = ((StudentActivity) getActivity()).menu.findItem(R.id.item_newHomework);
                            MenuItem newParticipation = ((StudentActivity) getActivity()).menu.findItem(R.id.item_newParticipation);
                            item_statistics.setVisible(false);
                            item_student.setVisible(true);
                            save_student.setVisible(true);
                            save_students.setVisible(true);
                            newAssignment.setVisible(false);
                            newHomework.setVisible(false);
                            newParticipation.setVisible(false);
                            delete_student.setVisible(false);
                        }
                        db.close();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if(listViewStudentNameList.size()==1){
                        listViewStudentNameList.remove(0);
                    }else{
                        for (int i = SelectedIdxToDelete.size()-1; i >=0; i--) {
                            listViewStudentNameList.remove((int) SelectedIdxToDelete.get(i));
                            arrayAdapter.notifyDataSetChanged();
                        }
                    }
                    arrayAdapter.notifyDataSetChanged();

                    dismiss();
                }
            });
        }

    }
}
