package com.example.kohistanbookingapp.util

const val PREFERENCE_NAME = "monitoringSys_preferences"
const val DB_NAME = "monitoring_system"
const val MQTT_CLIENT_ID_PREFIX = "MSys_Android_"
const val LOCATIONS_PUBLISH = "MSys/Locations"
const val LOCATIONS_RESPONSE = "MSys/Locations/Response"
const val LOCATION_ALERT = "MSys/HumanAlert"
const val DISMISS_PUBLISH = "MSys/Dismiss"
const val DISMISS_RESPONSE = "MSys/Dismiss/Response"

const val CAMERA_AUDIO_PERMISSION_CODE = 1001
const val LOCATION_PERMISSION_REQUEST_CODE = 1002

const val IPV4_PATTERN = """^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"""

const val IPV6_PATTERN =
    "^(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|" +
            "([0-9a-fA-F]{1,4}:){1,7}:|" +
            "([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|" +
            "([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|" +
            "([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|" +
            "([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|" +
            "([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|" +
            "[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|" +
            ":((:[0-9a-fA-F]{1,4}){1,7}|:)|" +
            "fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]+|::(ffff(:0{1,4})?:)?" +
            "((25[0-5]|(2[0-4]|1?[0-9])?[0-9]).){3}" +
            "(25[0-5]|(2[0-4]|1?[0-9])?[0-9])|" +
            "([0-9a-fA-F]{1,4}:){1,4}:" +
            "((25[0-5]|(2[0-4]|1?[0-9])?[0-9]).){3}" +
            "(25[0-5]|(2[0-4]|1?[0-9])?[0-9]))$"

const val DOMAIN_PATTERN = """^(?=.{1,255}$)([A-Za-z0-9-]+\.)*[A-Za-z0-9-]+(\.[A-Za-z]{2,})$"""
const val PORT_PATTERN = """^:(\d{1,5})$"""
const val DEFAULT_PASSWORD = "Tr@k098"
