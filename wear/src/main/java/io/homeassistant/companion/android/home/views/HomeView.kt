package io.homeassistant.companion.android.home.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.tiles.TileService
import io.homeassistant.companion.android.common.sensors.id
import io.homeassistant.companion.android.home.MainViewModel
import io.homeassistant.companion.android.theme.WearAppTheme
import io.homeassistant.companion.android.tiles.ShortcutsTile
import io.homeassistant.companion.android.tiles.TemplateTile
import io.homeassistant.companion.android.views.ChooseEntityView

private const val ARG_SCREEN_SENSOR_MANAGER_ID = "sensorManagerId"

private const val SCREEN_LANDING = "landing"
private const val SCREEN_ENTITY_DETAIL = "entity_detail"
private const val SCREEN_ENTITY_LIST = "entity_list"
private const val SCREEN_MANAGE_SENSORS = "manage_all_sensors"
private const val SCREEN_SINGLE_SENSOR_MANAGER = "sensor_manager"
private const val SCREEN_SETTINGS = "settings"
private const val SCREEN_SET_FAVORITES = "set_favorites"
private const val SCREEN_SET_TILE_SHORTCUTS = "set_tile_shortcuts"
private const val SCREEN_SELECT_TILE_SHORTCUT = "select_tile_shortcut"
private const val SCREEN_SET_TILE_TEMPLATE = "set_tile_template"
private const val SCREEN_SET_TILE_TEMPLATE_REFRESH_INTERVAL = "set_tile_template_refresh_interval"

const val DEEPLINK_SENSOR_MANAGER = "ha_wear://$SCREEN_SINGLE_SENSOR_MANAGER"

@Composable
fun LoadHomePage(
    mainViewModel: MainViewModel
) {
    var shortcutEntitySelectionIndex: Int by remember { mutableStateOf(0) }
    val context = LocalContext.current

    WearAppTheme {
        val swipeDismissableNavController = rememberSwipeDismissableNavController()
        SwipeDismissableNavHost(
            navController = swipeDismissableNavController,
            startDestination = SCREEN_LANDING
        ) {
            composable(SCREEN_LANDING) {
                MainView(
                    mainViewModel = mainViewModel,
                    favoriteEntityIds = mainViewModel.favoriteEntityIds.value,
                    onEntityClicked = { id, state -> mainViewModel.toggleEntity(id, state) },
                    onEntityLongClicked = { entityId ->
                        swipeDismissableNavController.navigate("$SCREEN_ENTITY_DETAIL/$entityId")
                    },
                    onRetryLoadEntitiesClicked = mainViewModel::loadEntities,
                    onSettingsClicked = { swipeDismissableNavController.navigate(SCREEN_SETTINGS) },
                    onTestClicked = { lists, order, filter ->
                        mainViewModel.entityLists.clear()
                        mainViewModel.entityLists.putAll(lists)
                        mainViewModel.entityListsOrder.clear()
                        mainViewModel.entityListsOrder.addAll(order)
                        mainViewModel.entityListFilter = filter
                        swipeDismissableNavController.navigate(SCREEN_ENTITY_LIST)
                    },
                    isHapticEnabled = mainViewModel.isHapticEnabled.value,
                    isToastEnabled = mainViewModel.isToastEnabled.value,
                    deleteFavorite = { id -> mainViewModel.removeFavoriteEntity(id) }
                )
            }
            composable("$SCREEN_ENTITY_DETAIL/{entityId}") {
                val entity = mainViewModel.entities[it.arguments?.getString("entityId")]
                if (entity != null) {
                    DetailsPanelView(
                        entity = entity,
                        onEntityToggled = { entityId, state ->
                            mainViewModel.toggleEntity(entityId, state)
                        },
                        onFanSpeedChanged = { speed ->
                            mainViewModel.setFanSpeed(
                                entity.entityId,
                                speed
                            )
                        },
                        onBrightnessChanged = { brightness ->
                            mainViewModel.setBrightness(
                                entity.entityId,
                                brightness
                            )
                        },
                        onColorTempChanged = { colorTemp ->
                            mainViewModel.setColorTemp(
                                entity.entityId,
                                colorTemp
                            )
                        },
                        isToastEnabled = mainViewModel.isToastEnabled.value,
                        isHapticEnabled = mainViewModel.isHapticEnabled.value
                    )
                }
            }
            composable(SCREEN_ENTITY_LIST) {
                EntityViewList(
                    entityLists = mainViewModel.entityLists,
                    entityListsOrder = mainViewModel.entityListsOrder,
                    entityListFilter = mainViewModel.entityListFilter,
                    onEntityClicked = { entityId, state ->
                        mainViewModel.toggleEntity(entityId, state)
                    },
                    onEntityLongClicked = { entityId ->
                        swipeDismissableNavController.navigate("$SCREEN_ENTITY_DETAIL/$entityId")
                    },
                    isHapticEnabled = mainViewModel.isHapticEnabled.value,
                    isToastEnabled = mainViewModel.isToastEnabled.value
                )
            }
            composable(SCREEN_SETTINGS) {
                SettingsView(
                    loadingState = mainViewModel.loadingState.value,
                    favorites = mainViewModel.favoriteEntityIds.value,
                    onClickSetFavorites = {
                        swipeDismissableNavController.navigate(
                            SCREEN_SET_FAVORITES
                        )
                    },
                    onClearFavorites = { mainViewModel.clearFavorites() },
                    onClickSetShortcuts = {
                        swipeDismissableNavController.navigate(
                            SCREEN_SET_TILE_SHORTCUTS
                        )
                    },
                    onClickSensors = {
                        swipeDismissableNavController.navigate(
                            SCREEN_MANAGE_SENSORS
                        )
                    },
                    onClickLogout = { mainViewModel.logout() },
                    isHapticEnabled = mainViewModel.isHapticEnabled.value,
                    isToastEnabled = mainViewModel.isToastEnabled.value,
                    onHapticEnabled = { mainViewModel.setHapticEnabled(it) },
                    onToastEnabled = { mainViewModel.setToastEnabled(it) }
                ) { swipeDismissableNavController.navigate(SCREEN_SET_TILE_TEMPLATE) }
            }
            composable(SCREEN_SET_FAVORITES) {
                SetFavoritesView(
                    mainViewModel,
                    mainViewModel.favoriteEntityIds.value
                ) { entityId, isSelected ->
                    if (isSelected) {
                        mainViewModel.addFavoriteEntity(entityId)
                    } else {
                        mainViewModel.removeFavoriteEntity(entityId)
                    }
                }
            }
            composable(SCREEN_SET_TILE_SHORTCUTS) {
                SetTileShortcutsView(
                    shortcutEntities = mainViewModel.shortcutEntities,
                    onShortcutEntitySelectionChange = {
                        shortcutEntitySelectionIndex = it
                        swipeDismissableNavController.navigate(SCREEN_SELECT_TILE_SHORTCUT)
                    },
                    isShowShortcutTextEnabled = mainViewModel.isShowShortcutTextEnabled.value,
                    onShowShortcutTextEnabled = {
                        mainViewModel.setShowShortcutTextEnabled(it)
                        TileService.getUpdater(context).requestUpdate(ShortcutsTile::class.java)
                    }
                )
            }
            composable(SCREEN_SELECT_TILE_SHORTCUT) {
                ChooseEntityView(
                    entitiesByDomainOrder = mainViewModel.entitiesByDomainOrder,
                    entitiesByDomain = mainViewModel.entitiesByDomain,
                    favoriteEntityIds = mainViewModel.favoriteEntityIds,
                    onNoneClicked = {
                        mainViewModel.clearTileShortcut(shortcutEntitySelectionIndex)
                        TileService.getUpdater(context).requestUpdate(ShortcutsTile::class.java)
                        swipeDismissableNavController.navigateUp()
                    },
                    onEntitySelected = { entity ->
                        mainViewModel.setTileShortcut(shortcutEntitySelectionIndex, entity)
                        TileService.getUpdater(context).requestUpdate(ShortcutsTile::class.java)
                        swipeDismissableNavController.navigateUp()
                    }
                )
            }
            composable(SCREEN_SET_TILE_TEMPLATE) {
                TemplateTileSettingsView(
                    templateContent = mainViewModel.templateTileContent.value,
                    refreshInterval = mainViewModel.templateTileRefreshInterval.value
                ) {
                    swipeDismissableNavController.navigate(
                        SCREEN_SET_TILE_TEMPLATE_REFRESH_INTERVAL
                    )
                }
            }
            composable(SCREEN_SET_TILE_TEMPLATE_REFRESH_INTERVAL) {
                RefreshIntervalPickerView(
                    currentInterval = mainViewModel.templateTileRefreshInterval.value
                ) {
                    mainViewModel.setTemplateTileRefreshInterval(it)
                    TileService.getUpdater(context).requestUpdate(TemplateTile::class.java)
                    swipeDismissableNavController.navigateUp()
                }
            }
            composable(route = SCREEN_MANAGE_SENSORS) {
                SensorsView(onClickSensorManager = {
                    swipeDismissableNavController.navigate("$SCREEN_SINGLE_SENSOR_MANAGER/${it.id()}")
                })
            }
            composable(
                route = "$SCREEN_SINGLE_SENSOR_MANAGER/{$ARG_SCREEN_SENSOR_MANAGER_ID}",
                arguments = listOf(
                    navArgument(name = ARG_SCREEN_SENSOR_MANAGER_ID) {
                        type = NavType.StringType
                    }
                ),
                deepLinks = listOf(
                    navDeepLink { uriPattern = "$DEEPLINK_SENSOR_MANAGER/{$ARG_SCREEN_SENSOR_MANAGER_ID}" }
                )
            ) { backStackEntry ->
                val sensorManagerId =
                    backStackEntry.arguments?.getString(ARG_SCREEN_SENSOR_MANAGER_ID)
                val sensorManager = getSensorManagers().first { sensorManager ->
                    sensorManager.id() == sensorManagerId
                }
                mainViewModel.updateAllSensors(sensorManager)
                SensorManagerUi(
                    allSensors = mainViewModel.sensors.value,
                    allAvailSensors = mainViewModel.availableSensors,
                    sensorManager = sensorManager,
                ) { sensorId, isEnabled ->
                    mainViewModel.enableDisableSensor(sensorManager, sensorId, isEnabled)
                }
            }
        }
    }
}
