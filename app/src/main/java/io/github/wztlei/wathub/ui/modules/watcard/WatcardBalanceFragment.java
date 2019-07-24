package io.github.wztlei.wathub.ui.modules.watcard;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.deange.uwaterlooapi.UWaterlooApi;
import com.deange.uwaterlooapi.annotations.ModuleFragment;
import com.deange.uwaterlooapi.model.watcard.Watcard;
import com.deange.uwaterlooapi.model.watcard.WatcardCredentials;

import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.controller.WatcardManager;
import io.github.wztlei.wathub.ui.ModuleAdapter;
import io.github.wztlei.wathub.ui.modules.ModuleType;
import io.github.wztlei.wathub.ui.modules.base.AbstractApiModuleFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;

@ModuleFragment(
    path = "/watcard/balance",
    layout = R.layout.module_watcard_balance
)
public class WatcardBalanceFragment extends AbstractApiModuleFragment<Watcard> {

    @BindView(R.id.watcard_balance_total)
    TextView mTotalView;
    @BindView(R.id.watcard_balance_list)
    ListView mBalancesListView;

    private Watcard mWatcard;

    @Override
    protected View getContentView(final LayoutInflater inflater, final ViewGroup parent) {
        final View view = inflater.inflate(R.layout.fragment_watcard_balance, parent, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_edit_watcard, menu);

        final WatcardCredentials credentials = getApi().getWatcardCredentials();
        if (credentials != null) {
            menu.findItem(R.id.menu_edit_watcard).setTitle(credentials.studentNumber());
        }

        syncRefreshMenuItem(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.menu_edit_watcard) {
            showWatcardEditDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showWatcardEditDialog() {
        final LayoutInflater inflater = LayoutInflater.from(getContext());
        final View view = inflater.inflate(R.layout.dialog_watcard_editor, null);

        final EditText studentNumberText = view.findViewById(
                R.id.dialog_watcard_student_number);
        final EditText pinText = view.findViewById(R.id.dialog_watcard_pin);

        final WatcardCredentials credentials = getApi().getWatcardCredentials();
        if (credentials != null) {
            studentNumberText.setText(credentials.studentNumber());
            pinText.setText(credentials.pin());
            pinText.requestFocus();
            pinText.setSelection(pinText.getText().length());
        }

        new AlertDialog.Builder(getContext())
                .setIcon(R.drawable.ic_watcard)
                .setTitle(R.string.watcard_editor_title)
                .setView(view)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (d, which) ->
                        saveWatcard(
                                studentNumberText.getText().toString(),
                                pinText.getText().toString()))
                .show();
    }

    private void saveWatcard(final String studentNumber, final String pin) {
        final WatcardCredentials credentials = WatcardCredentials.create(studentNumber, pin);
        WatcardManager.getInstance().saveCredentials(credentials);
        getApi().setWatcardCredentials(credentials);

        doRefresh();
    }

    @Override
    public Call<Watcard> onLoadData(final UWaterlooApi api) {
        return api.Watcard.balances();
    }

    @Override
    protected void onNullResponseReceived() {}

    @Override
    public void onBindData(final Watcard data) {
        mWatcard = data;

        mTotalView.setText(mWatcard.totalFormatted());
        mBalancesListView.setAdapter(new BalancesAdapter(getContext()));
    }

    @Override
    public String getContentType() {
        return ModuleType.WATCARD_BALANCE;
    }

    @Override
    public String getToolbarTitle() {
        return getString(R.string.title_watcard_balance);
    }

    @Override
    public float getToolbarElevationPx() {
        return 0;
    }

    private class BalancesAdapter extends ModuleAdapter {

        BalancesAdapter(final Context context) {
            super(context);
        }

        @Override
        public void bindView(final Context context, final int position, final View view) {
            final Watcard.Row row = getItem(position);

            ((TextView) view.findViewById(android.R.id.text1)).setText(row.account());
            ((TextView) view.findViewById(android.R.id.text2)).setText(row.amountFormatted());
        }

        @Override
        public int getCount() {
            return (mWatcard != null) ? mWatcard.accounts().size() : 0;
        }

        @Override
        public Watcard.Row getItem(final int position) {
            return mWatcard.accounts().get(position);
        }

        @Override
        public int getListItemLayoutId() {
            return R.layout.list_item_watcard_balance;
        }
    }
}
