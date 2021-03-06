package teclan.lvzaotou.example.api.achieve;

import com.google.inject.Inject;

import teclan.lvzaotou.core.api.achieve.AbstractServiceApis;
import teclan.lvzaotou.core.service.db.ActiveJdbcService;
import teclan.lvzaotou.example.api.ContentServerApis;
import teclan.lvzaotou.example.model.ContentRecord;
import teclan.lvzaotou.example.service.ContentRecordService;

public class DefaultContentServerApis extends AbstractServiceApis<ContentRecord>implements ContentServerApis {

	@Inject
	private ContentRecordService service;

	@Override
	public ActiveJdbcService<ContentRecord> getService() {
		return service;
	}

	@Override
	public String getResource() {
		return "contents";
	}

	@Override
	protected void customizeApis() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getSingleResource() {
		return "content";
	}

}
