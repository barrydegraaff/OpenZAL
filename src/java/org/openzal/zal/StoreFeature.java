package org.openzal.zal;

/* $if ZimbraVersion >= 7.2.0 $ */
import com.zimbra.cs.store.StoreManager;
/* $endif $ */
import org.jetbrains.annotations.NotNull;

public enum StoreFeature
{
  /* $if ZimbraVersion >= 7.2.0 $ */
  BULK_DELETE(StoreManager.StoreFeature.BULK_DELETE),
  CENTRALIZED(StoreManager.StoreFeature.CENTRALIZED),
  /* $if ZimbraVersion >= 8.0.0 $ */
  RESUMABLE_UPLOAD(StoreManager.StoreFeature.RESUMABLE_UPLOAD),
  SINGLE_INSTANCE_SERVER_CREATE(StoreManager.StoreFeature.SINGLE_INSTANCE_SERVER_CREATE);
  /* $else $
  RESUMABLE_UPLOAD(null),
  SINGLE_INSTANCE_SERVER_CREATE(null);
  /* $endif $
  /* $else $
  BULK_DELETE(null),
  CENTRALIZED(null),
  RESUMABLE_UPLOAD(null),
  SINGLE_INSTANCE_SERVER_CREATE(null);
  /* $endif $ */

  /* $if ZimbraVersion >= 7.2.0 $ */
  private final StoreManager.StoreFeature mStoreFeature;
  /* $else $
  private final Object                    mStoreFeature;
  /* $endif $ */

  StoreFeature(Object storeFeature)
  {
    /* $if ZimbraVersion >= 7.2.0 $ */
    mStoreFeature = (StoreManager.StoreFeature) storeFeature;
    /* $else $
    mStoreFeature = null;
    /* $endif $ */
  }

  protected <T> T toZimbra(@NotNull  Class<T> className)
  {
    if (mStoreFeature == null)
    {
      return null;
    }

    return className.cast(mStoreFeature);
  }

  public static StoreFeature fromZimbra(Object storeFeature)
  {
    /* $if ZimbraVersion >= 7.2.0 $ */
    switch ((StoreManager.StoreFeature) storeFeature)
    {
      case BULK_DELETE:
        return BULK_DELETE;
      case CENTRALIZED:
        return CENTRALIZED;
      /* $if ZimbraVersion >= 8.0.0 $ */
      case RESUMABLE_UPLOAD:
        return RESUMABLE_UPLOAD;
      case SINGLE_INSTANCE_SERVER_CREATE:
        return SINGLE_INSTANCE_SERVER_CREATE;
      /* $endif $ */
      default:
        throw new RuntimeException();
    }
    /* $else $
    throw new UnsupportedOperationException();
    /* $endif $ */
  }
}