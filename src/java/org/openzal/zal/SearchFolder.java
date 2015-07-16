/*
 * ZAL - The abstraction layer for Zimbra.
 * Copyright (C) 2015 ZeXtras S.r.l.
 *
 * This file is part of ZAL.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZAL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.openzal.zal;

import org.jetbrains.annotations.NotNull;

public class SearchFolder extends Folder
{
  @NotNull private final com.zimbra.cs.mailbox.SearchFolder mSearchFolder;

  public SearchFolder(@NotNull Object item)
  {
    super(item);
    mSearchFolder = (com.zimbra.cs.mailbox.SearchFolder) item;
  }

  public String getQuery()
  {
    return mSearchFolder.getQuery();
  }

  public String getReturnTypes()
  {
    return mSearchFolder.getReturnTypes();
  }

  public String getSortField()
  {
    return mSearchFolder.getSortField();
  }

  public String getName()
  {
    return mSearchFolder.getName();
  }
}
