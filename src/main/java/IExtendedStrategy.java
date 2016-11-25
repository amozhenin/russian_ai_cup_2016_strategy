/**
 * Created by dvorkin on 24.11.2016.
 */
public interface IExtendedStrategy extends Strategy {
    DataStorage getDataStorage();
    void setDataStorage(DataStorage storage);
    void finish();
}
