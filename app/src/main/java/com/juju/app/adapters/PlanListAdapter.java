package com.juju.app.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.juju.app.R;
import com.juju.app.entity.Plan;
import com.juju.app.utils.ViewHolderUtil;
import com.juju.app.view.dialog.WarnTipDialog;

import java.text.SimpleDateFormat;
import java.util.List;

public class PlanListAdapter extends BaseSwipeAdapter {

    private Context context;
    private List<Plan> planList;
    private SimpleDateFormat dateFormat;

    private int deleteIndex;
    private DialogInterface.OnClickListener onDeleteClick;

    public PlanListAdapter(Context context, List<Plan> planList) {
        this.context = context;
        this.planList = planList;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        initDeleteClick();
    }

    private void initDeleteClick() {
        onDeleteClick = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        };
    }


    @Override
    public int getCount() {
        return planList.size();
    }

    @Override
    public Object getItem(int position) {
        return planList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public List<Plan> getPlanList(){
        return planList;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    @Override
    public View generateView(int position, ViewGroup parent) {
        return  LayoutInflater.from(context).inflate(R.layout.plan_item, parent, false);
    }

    @Override
    public void fillValues(int position, View convertView) {
        renderPlanList(position, convertView);
    }

    public void renderPlanList(final int position, View convertView) {

        TextView txt_time = ViewHolderUtil.get(convertView, R.id.txt_time);
        TextView txt_address = ViewHolderUtil.get(convertView, R.id.txt_address);
        TextView txt_description = ViewHolderUtil.get(convertView, R.id.txt_description);
        TextView txt_del = ViewHolderUtil.get(convertView, R.id.txt_del);

        final Plan plan = planList.get(position);
        txt_time.setText(dateFormat.format(plan.getStartTime()));
        txt_address.setText(plan.getAddress());
        txt_description.setText(plan.getDesc());
        txt_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteIndex = position;
                WarnTipDialog tipdialog = new WarnTipDialog(context,"您确定要删除该方案吗？");
                tipdialog.setBtnOkLinstener(onDeleteClick);
                tipdialog.show();
            }
        });

    }


}
