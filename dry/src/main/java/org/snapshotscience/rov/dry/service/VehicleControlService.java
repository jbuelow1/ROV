package org.snapshotscience.rov.dry.service;

/* This file is part of WAHU ROV Software.
 *
 * WAHU ROV Software is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * WAHU ROV Software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WAHU ROV Software.  If not, see <https://www.gnu.org/licenses/>.
 */

import java.util.List;
import java.util.UUID;
import org.snapshotscience.rov.common.command.Command;
import org.snapshotscience.rov.common.response.Response;
import org.snapshotscience.rov.common.response.VehicleCapabilities;

/**
 * @author Jacob Buelow
 * @author Brian Wachsmuth
 */
public interface VehicleControlService {

  boolean activeConnections();

  Response sendCommand(UUID vehicleId, Command command);

  List<VehicleCapabilities> getAttatchedVehicles();
}
