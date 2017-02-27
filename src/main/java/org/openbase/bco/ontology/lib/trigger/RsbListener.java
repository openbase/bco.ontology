/**
 * ==================================================================
 *
 * This file is part of org.openbase.bco.ontology.lib.
 *
 * org.openbase.bco.ontology.lib is free software: you can redistribute it and modify
 * it under the terms of the GNU General Public License (Version 3)
 * as published by the Free Software Foundation.
 *
 * org.openbase.bco.ontology.lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with org.openbase.bco.ontology.lib. If not, see <http://www.gnu.org/licenses/>.
 * ==================================================================
 */
package org.openbase.bco.ontology.lib.trigger;

import org.openbase.bco.ontology.lib.config.CategoryConfig.ChangeCategory;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.extension.rsb.com.RSBFactoryImpl;
import org.openbase.jul.extension.rsb.iface.RSBListener;
import org.openbase.jul.pattern.ObservableImpl;

import java.util.Collection;

/**
 * @author agatting on 27.02.17.
 */
public interface RsbListener {

    ObservableImpl<Collection<ChangeCategory>> changeCategoryObservable = new ObservableImpl<>();

    static void activateRsbListener(final String scope) throws CouldNotPerformException {

        try {
            final RSBListener rsbListener = RSBFactoryImpl.getInstance().createSynchronizedListener(scope);
            rsbListener.activate();
            rsbListener.addHandler(event -> {
                try {
                    changeCategoryObservable.notifyObservers((Collection<ChangeCategory>) event.getData()); //TODO check cast
                } catch (MultiException e) {

                }
//                LOGGER.info("receive event:" + event.getData());
            }, false);

        } catch (InterruptedException e) {
            throw new CouldNotPerformException("Could not create RSB listener!", e);
        }
    }
}
