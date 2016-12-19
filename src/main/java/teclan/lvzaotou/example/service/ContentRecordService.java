package teclan.lvzaotou.example.service;

import com.google.inject.ImplementedBy;

import teclan.lvzaotou.core.service.db.ActiveJdbcService;
import teclan.lvzaotou.example.model.ContentRecord;
import teclan.lvzaotou.example.service.achieve.DefaultContentRecordService;

@ImplementedBy(DefaultContentRecordService.class)
public interface ContentRecordService extends ActiveJdbcService<ContentRecord> {

}
