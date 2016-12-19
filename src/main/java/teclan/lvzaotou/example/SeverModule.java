package teclan.lvzaotou.example;

import com.google.inject.AbstractModule;

import teclan.lvzaotou.core.db.Database;
import teclan.lvzaotou.core.provider.DatabaseProvider;

public class SeverModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Database.class).toProvider(DatabaseProvider.class);
    }
}
