///    @author: Pawel Palka
///    @email: ppalka@sosoftware.pl    // company
///    @email: fractalord@gmail.com    // private

#include "pebble.h"

static Window *login_window;
static SimpleMenuLayer *login_menu_layer;
static TextLayer *login_uppertext_layer;
static TextLayer *login_lowertext_layer;
static PropertyAnimation *login_property_animation;

static Window *user_window;
static SimpleMenuLayer *user_menu_layer;
static TextLayer *user_textbar_layer;
static TextLayer *user_left_text_layer;
static TextLayer *user_right_text_layer;

static Window *beacons_window;
static MenuLayer *beacons_menu_layer;
static TextLayer *beacons_textbar_layer;
static BitmapLayer *beacons_downloading_sign_layer;

static Window *coworkers_window;
static MenuLayer *coworkers_menu_layer;
static TextLayer *coworkers_textbar_layer;
static TextLayer *coworkers_text_layer;
static BitmapLayer *coworkers_downloading_sign_layer;

static Window *achievements_window;
static MenuLayer *achievements_menu_layer;
static TextLayer *achievements_textbar_layer;
static BitmapLayer *achievements_downloading_sign_layer;

static Window *achievement_details_window;
static TextLayer *achievement_details_textbar_layer;
static TextLayer *achievement_details_title_text_layer;
static TextLayer *achievement_details_content_text_layer;
static ScrollLayer *achievement_details_scroll_layer;
static BitmapLayer *achievement_details_downloading_sign_layer;

static GBitmap *beacons_icon;
static GBitmap *user_icon;
static GBitmap *achievements_icon;
static GBitmap *downloading_sign_image;

typedef struct {
    char *name;
    char *location;
    int points;
    int rank;
    int beacons;
    int achievements;
    int level;
    bool logged_on;
} User;

typedef struct {
    int id;
    char *name;
    int coworkers;
} Beacon;

typedef struct {
    int id;
    char *name;
    int beacon_id;
} Coworker;

typedef struct {
    int id;
    char *name;
    int parts;
} Achievement;

static Beacon *current_beacon;
static Beacon *previous_beacon;
static Achievement *current_achievement;
static Achievement *previous_achievement;

static User user;
static Beacon **beacons;
static Coworker **coworkers;
static Achievement **achievements;

uint16_t num_beacons;
uint16_t num_coworkers;
uint16_t max_coworkers;
uint16_t num_achievements;
uint16_t max_achievements;

uint8_t last_request;

static bool is_downloading;
static bool achievements_first_time;
static bool animated = true;

static char current_achievement_description[255];

static int textbar_height = 24;

static uint16_t get_num_beacons(struct MenuLayer* menu_layer, uint16_t section_index, void *callback_context) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "get_num_beacons() return: %u",num_beacons);
    return num_beacons;
}

static uint16_t get_num_coworkers(struct MenuLayer* menu_layer, uint16_t section_index, void *callback_context) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "get_num_coworkers() return: %u",num_coworkers);
    return num_coworkers;
}

static uint16_t get_num_achievements(struct MenuLayer* menu_layer, uint16_t section_index, void *callback_context) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "get_num_achievements() return: %u",num_achievements);
    return num_achievements;
}

static int beacons_compare(const void *b1, const void *b2) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "beacons_compare()");
    char *n1 = (*(Beacon**)b1)->name;
    char *n2 = (*(Beacon**)b2)->name;
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "    d1: %i | d2: %i",d1,d2);
    return strcmp(n1,n2);
}

static bool update_beacons_table(Beacon *new_beacon) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "update_beacons_table() start new_beacon->name: %s",new_beacon->name);
    if(beacons==NULL) {
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "    table empty, allocating new table");
        beacons = (Beacon**)malloc(user.beacons*sizeof(Beacon*));
        char *new_name = (char*)malloc((strlen(new_beacon->name)+1)*sizeof(char));
        strcpy(new_name,new_beacon->name);
        beacons[0] = new_beacon;
        beacons[0]->name = new_name;
        num_beacons = 0;
        num_beacons++;
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "    added first beacon: %s",beacons[0]->name);
        return true;
    }
    else {
        int size = num_beacons;
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "    table_size: %u",size);
        int i;
        for(i=0; i<size && i<user.beacons; ++i) {
            // if that beacon already exists in the table
            if(beacons[i]->id==new_beacon->id) {
                // overwrite coworkers counter (probably faster than checking them and overwriting if changed)
                if(beacons[i]->coworkers!=new_beacon->coworkers) {
                    //APP_LOG(APP_LOG_LEVEL_DEBUG, "    beacon found but changed");
                    beacons[i]->coworkers=new_beacon->coworkers;
                    if(new_beacon) {
                        free(new_beacon);
                        new_beacon = NULL;
                    }
                    return true;
                }
                else {
                    //APP_LOG(APP_LOG_LEVEL_DEBUG, "    beacon found but not changed, rejecting");
                    if(new_beacon) {
                        free(new_beacon);
                        new_beacon = NULL;
                    }
                    return false;
                }
            }
        }
        // add new if not found in table
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "    beacon not found, adding new");
        char *new_name = (char*)malloc((strlen(new_beacon->name)+1)*sizeof(char));
        strcpy(new_name,new_beacon->name);
        beacons[size] = new_beacon;
        beacons[size]->name = new_name;
        num_beacons++;
        qsort(beacons,size+1,sizeof(Beacon*),beacons_compare);
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "update_beacons_table() end");
        return true;
    }
}

static bool update_coworkers_table(Coworker *new_coworker) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "update_coworkers_table() start: %s",new_coworker->name);
    if(current_beacon->id==new_coworker->beacon_id) {
        if(coworkers==NULL) {
            if(current_beacon->coworkers==0)
                max_coworkers = 1;
            else
                max_coworkers = current_beacon->coworkers+1;
            //APP_LOG(APP_LOG_LEVEL_DEBUG, "    table empty, allocating new table, size: %u",max_coworkers);
            coworkers = (Coworker**)malloc(max_coworkers*sizeof(Coworker*));
            char *new_name = (char*)malloc((strlen(new_coworker->name)+1)*sizeof(char));
            strcpy(new_name,new_coworker->name);
            coworkers[0] = new_coworker;
            coworkers[0]->name = new_name;
            num_coworkers = 1;
            //APP_LOG(APP_LOG_LEVEL_DEBUG, "    added first coworker: %s",coworkers[0]->name);
            return true;
        }
        else {
            int size = num_coworkers;
            //APP_LOG(APP_LOG_LEVEL_DEBUG, "    num_coworkers: %u | max_coworkers: %u",num_coworkers,max_coworkers);
            int i;
            for(i=0; i<size; ++i) {
                // if that coworker already exists in the table
                if(coworkers[i]->id==new_coworker->id) {
                    //APP_LOG(APP_LOG_LEVEL_DEBUG, "    coworker found, rejecting");
                    if(new_coworker) {
                        free(new_coworker);
                        new_coworker = NULL;
                    }
                    return false;
                }
            }
            if(i<max_coworkers) {
                // add new if not found in table
                //APP_LOG(APP_LOG_LEVEL_DEBUG, "    coworker not found, adding new, name_size: %u",strlen(new_coworker->name));
                char *new_name = (char*)malloc((strlen(new_coworker->name)+1)*sizeof(char));
                strcpy(new_name,new_coworker->name);
                coworkers[i] = new_coworker;
                coworkers[i]->name = new_name;
                num_coworkers++;
                //APP_LOG(APP_LOG_LEVEL_DEBUG, "update_coworkers_table() end");
                return true;
            }
            // reallocate table and increase its size if new element doesn't fit
            else {
                //APP_LOG(APP_LOG_LEVEL_DEBUG, "    coworker not found, reallocating table and adding new");
                max_coworkers += 2;
                Coworker **new_table = (Coworker**)realloc(coworkers,max_coworkers*sizeof(Coworker*));
                if(new_table) {
                    coworkers = new_table;
                    char *new_name = (char*)malloc((strlen(new_coworker->name)+1)*sizeof(char));
                    strcpy(new_name,new_coworker->name);
                    coworkers[i] = new_coworker;
                    coworkers[i]->name = new_name;
                    num_coworkers++;
                }
            }
            //APP_LOG(APP_LOG_LEVEL_DEBUG, "update_coworkers_table() end");
            return true;
        }
    }
    else {
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "    beacon_id differs, rejecting");
        if(new_coworker) {
            free(new_coworker);
            new_coworker = NULL;
        }
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "update_coworkers_table() end");
    }
    return false;
}

static bool update_achievements_table(Achievement *new_achievement) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "update_achievements_table() start id: %u | name: %s | description: %s",new_achievement->id,new_achievement->name,new_achievement->description);
    if(achievements==NULL) {
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "    table empty, allocating new table");
        if(user.achievements<=0)
            max_achievements = 1;
        else
            max_achievements = user.achievements;
        achievements = (Achievement**)malloc(max_achievements*sizeof(Achievement*));
        char *new_name = (char*)malloc((strlen(new_achievement->name)+1)*sizeof(char));
        strcpy(new_name,new_achievement->name);
        achievements[0] = new_achievement;
        achievements[0]->name = new_name;
        num_achievements = 1;
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "    added first achievement: %s",achievements[0]->name);
        return true;
    }
    else {
        int size = num_achievements;
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "    table_size: %u",size);
        int i;
        for(i=0; i<size && i<max_achievements; ++i) {
            // if that achievement already exists in the table
            if(achievements[i]->id==new_achievement->id) {
                //APP_LOG(APP_LOG_LEVEL_DEBUG, "    achievement found, rejecting");
                // we assume that description of the achievement doesn't change
                if(new_achievement) {
                    free(new_achievement);
                    new_achievement = NULL;
                }
                return false;
            }
        }
        // add new if not found in table
        if(i<max_achievements) {
            //APP_LOG(APP_LOG_LEVEL_DEBUG, "    achievement not found, adding new");
            char *new_name = (char*)malloc((strlen(new_achievement->name)+1)*sizeof(char));
            strcpy(new_name,new_achievement->name);
            achievements[i] = new_achievement;
            achievements[i]->name = new_name;
            num_achievements++;
        }
        // reallocate table and increase its size twice if new element doesn't fit
        else {
            //APP_LOG(APP_LOG_LEVEL_DEBUG, "    achievement not found, reallocating table and adding new");
            max_achievements += 2;
            Achievement **new_table = (Achievement**)realloc(achievements,max_achievements*sizeof(Achievement*));
            if(new_table) {
                achievements = new_table;
                char *new_name = (char*)malloc((strlen(new_achievement->name)+1)*sizeof(char));
                strcpy(new_name,new_achievement->name);
                achievements[i] = new_achievement;
                achievements[i]->name = new_name;
                num_achievements++;
            }
        }
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "update_achievements_table() end");
        return true;
    }
}

static void clear_beacons_table() {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "clear_beacons_table() start");
    if(beacons) {
        int size = num_beacons;
        int i;
        for(i=0; i<size; ++i) {
            if(beacons[i]) {
                if(beacons[i]->name) {
                    free(beacons[i]->name);
                    beacons[i]->name = NULL;
                }
                free(beacons[i]);
                beacons[i] = NULL;
            }
        }
        free(beacons);
        beacons = NULL;
    }
    num_beacons = 0;
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "clear_beacons_table() end");
}

static void clear_coworkers_table() {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "clear_coworkers_table() start | size: %u",num_coworkers);
    if(coworkers) {
        int size = num_coworkers;
        int i;
        for(i=0; i<size && i<max_coworkers; ++i) {
            //APP_LOG(APP_LOG_LEVEL_DEBUG, "clear_coworkers_table() i: %u",i);
            if(coworkers[i]) {
                if(coworkers[i]->name) {
                    free(coworkers[i]->name);
                    coworkers[i]->name = NULL;
                }
                free(coworkers[i]);
                coworkers[i] = NULL;
            }
        }
        free(coworkers);
        coworkers = NULL;
    }
    num_coworkers = 0;
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "clear_coworkers_table() end");
}

static void clear_achievements_table() {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "clear_achievements_table() start");
    if(achievements) {
        int size = num_achievements;
        int i;
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "clear_achievements_table() size: %i",size);
        for(i=0; i<size && i<max_achievements; ++i) {
            if(achievements[i]) {
                if(achievements[i]->name) {
                    free(achievements[i]->name);
                    achievements[i]->name = NULL;
                }
                free(achievements[i]);
                achievements[i] = NULL;
            }
        }
        free(achievements);
        achievements = NULL;
    }
    num_achievements = 0;
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "clear_achievements_table() end");
}

static bool pop_coworker_from_table(int id) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "pop_coworker_from_table() start");
    int i=0, size = num_coworkers;
    while(coworkers[i]->id!=id) {
        i++;
        if(i==size)
            return false;
    }
    if(coworkers[i]) {
        if(coworkers[i]->name)
            free(coworkers[i]->name);
        free(coworkers[i]);
    }
    for( ; i<size-1; ++i)
        coworkers[i] = coworkers[i+1];
    coworkers[i] = NULL;
    num_coworkers--;
    if(coworkers[0]==NULL)
        clear_coworkers_table();
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "pop_coworker_from_table() end");
    return true;
}

static void set_textbar_layer(Layer *window_layer, TextLayer **textbar_layer) {
    GRect textbar_bounds = layer_get_bounds(window_layer);
    textbar_bounds.size.h = textbar_height;
    *textbar_layer = text_layer_create(textbar_bounds);
    text_layer_set_font(*textbar_layer,fonts_get_system_font(FONT_KEY_GOTHIC_18));
    text_layer_set_text_alignment(*textbar_layer, GTextAlignmentCenter);
    text_layer_set_background_color(*textbar_layer,GColorBlack);
    text_layer_set_text_color(*textbar_layer,GColorWhite);
}

static void set_downloading_sign_layer(Layer *window_layer, BitmapLayer **downloading_sign_layer) {
    GRect downloading_sign_layer_bounds = layer_get_bounds(window_layer);
    downloading_sign_layer_bounds.origin.x = downloading_sign_layer_bounds.size.w-textbar_height;
    downloading_sign_layer_bounds.size.h = textbar_height;
    downloading_sign_layer_bounds.size.w = textbar_height;
    *downloading_sign_layer = bitmap_layer_create(downloading_sign_layer_bounds);
    bitmap_layer_set_bitmap(*downloading_sign_layer,downloading_sign_image);
    bitmap_layer_set_alignment(*downloading_sign_layer,GAlignCenter);
}

static void fire_login_animation();

enum {
    // request keys
    REQUEST_TYPE = 1,
    REQUEST_QUERY = 2,
    
    // request values
    REQUEST_USER = 1,
    REQUEST_BEACONS = 2,
    REQUEST_COWORKERS = 3,
    REQUEST_ACHIEVEMENT_HEADERS = 4,
    REQUEST_ACHIEVEMENT_CONTENT = 5,
    
    // response keys
    RESPONSE_TYPE = 1,
    USER_NAME = 2,
    USER_LOCATION = 3,
    USER_POINTS = 4,
    USER_RANK = 5,
    USER_BEACONS = 6,
    USER_ACHIEVEMENTS = 7,
    USER_LEVEL = 8,
    BEACON_ID = 2,
    BEACON_NAME = 3,
    BEACON_COWORKERS = 4,
    COWORKER_ID = 2,
    COWORKER_NAME = 3,
    COWORKER_BEACON_ID = 4,
    ACHIEVEMENT_ID = 2,
    ACHIEVEMENT_NAME = 3,
    ACHIEVEMENT_DESCRIPTION = 3,
    ACHIEVEMENT_NUMBER = 4,
    
    // response values
    RESPONSE_USER = 1,
    RESPONSE_BEACON = 2,
    RESPONSE_COWORKER = 3,
    RESPONSE_ACHIEVEMENT_HEADER = 4,
    RESPONSE_ACHIEVEMENT_CONTENT = 5,
    RESPONSE_COWORKER_POP = 6
};

///////////////////////////////////// COMMUNICATION
char * translate_result(AppMessageResult result) {
    switch (result) {
        case APP_MSG_OK: return "APP_MSG_OK";
        case APP_MSG_SEND_TIMEOUT: return "APP_MSG_SEND_TIMEOUT";
        case APP_MSG_SEND_REJECTED: return "APP_MSG_SEND_REJECTED";
        case APP_MSG_NOT_CONNECTED: return "APP_MSG_NOT_CONNECTED";
        case APP_MSG_APP_NOT_RUNNING: return "APP_MSG_APP_NOT_RUNNING";
        case APP_MSG_INVALID_ARGS: return "APP_MSG_INVALID_ARGS";
        case APP_MSG_BUSY: return "APP_MSG_BUSY";
        case APP_MSG_BUFFER_OVERFLOW: return "APP_MSG_BUFFER_OVERFLOW";
        case APP_MSG_ALREADY_RELEASED: return "APP_MSG_ALREADY_RELEASED";
        case APP_MSG_CALLBACK_ALREADY_REGISTERED: return "APP_MSG_CALLBACK_ALREADY_REGISTERED";
        case APP_MSG_CALLBACK_NOT_REGISTERED: return "APP_MSG_CALLBACK_NOT_REGISTERED";
        case APP_MSG_OUT_OF_MEMORY: return "APP_MSG_OUT_OF_MEMORY";
        case APP_MSG_CLOSED: return "APP_MSG_CLOSED";
        case APP_MSG_INTERNAL_ERROR: return "APP_MSG_INTERNAL_ERROR";
        default: return "UNKNOWN ERROR";
    }
}

static void send_simple_request(int8_t request) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "send_simple_request() Request: %u start",request);
    DictionaryIterator *iter;
    app_message_outbox_begin(&iter);
    
    Tuplet value = TupletInteger(REQUEST_TYPE,request);
    dict_write_tuplet(iter,&value);
    dict_write_end(iter);
    
    last_request = request;
    app_message_outbox_send();
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "send_simple_request() Request: %u end",request);
}

static void send_query_request(int8_t request, int query) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "send_query_request() Request: %u Query: %u start",request,query);
    DictionaryIterator *iter;
    app_message_outbox_begin(&iter);
    
    Tuplet value1 = TupletInteger(REQUEST_TYPE,request);
    Tuplet value2 = TupletInteger(REQUEST_QUERY,query);
    dict_write_tuplet(iter,&value1);
    dict_write_tuplet(iter,&value2);
    dict_write_end(iter);
    
    last_request = request;
    
    app_message_outbox_send();
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "send_query_request() Request: %u Query: %u end",request,query);
}

void out_sent_handler(DictionaryIterator *sent, void *context) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "out_sent_handler()");
}

void out_failed_handler(DictionaryIterator *failed, AppMessageResult reason, void *context) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "out_failed_handler() Reason: %s",translate_result(reason));
}

void in_received_handler(DictionaryIterator *iter, void *context) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "in_received_handler() start dictionary size: %u",(uint16_t)dict_size(iter));
    Tuple *receiving_type = dict_find(iter,RESPONSE_TYPE);
    if(receiving_type) {
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "Receiving_type: %u",*(receiving_type->value->data));
        if(*(receiving_type->value->data)==RESPONSE_USER) {
            //APP_LOG(APP_LOG_LEVEL_DEBUG, "Starting receiving user");
            Tuple *name = dict_find(iter,USER_NAME);
            Tuple *location = dict_find(iter,USER_LOCATION);
            Tuple *points = dict_find(iter,USER_POINTS);
            Tuple *rank = dict_find(iter,USER_RANK);
            Tuple *beacons = dict_find(iter,USER_BEACONS);
            Tuple *achievements = dict_find(iter,USER_ACHIEVEMENTS);
            Tuple *level = dict_find(iter,USER_LEVEL);
            if(level && achievements && beacons && rank && points && location && name) {
                if(user.name==NULL) {
                    char *new_name = (char*)malloc((strlen(name->value->cstring)+1)*sizeof(char));
                    strcpy(new_name,name->value->cstring);
                    user.name = new_name;
                    char *new_location = (char*)malloc((strlen(location->value->cstring)+1)*sizeof(char));
                    strcpy(new_location,location->value->cstring);
                    user.location = new_location;
                    bluetooth_connection_service_unsubscribe();
                }
                else {
                    if(strcmp(user.name,name->value->cstring)!=0) {
                        char *new_name = (char*)malloc((strlen(name->value->cstring)+1)*sizeof(char));
                        strcpy(new_name,name->value->cstring);
                        free(user.name);
                        user.name = new_name;
                    }
                    if(strcmp(user.location,location->value->cstring)!=0) {
                        char *new_location = (char*)malloc((strlen(location->value->cstring)+1)*sizeof(char));
                        strcpy(new_location,location->value->cstring);
                        free(user.location);
                        user.location = new_location;
                    }
                }
                user.points = *(points->value->data);
                user.rank = *(rank->value->data);
                user.beacons = *(beacons->value->data);
                user.achievements = *(achievements->value->data);
                user.level = *(level->value->data);
                vibes_short_pulse();
                if(!user.logged_on)
                    fire_login_animation();
                else {
                    static char text_buffer[40];
                    if(strlen(user.location)>1)
                        snprintf(text_buffer,40,"in %s",user.location);
                    else
                        snprintf(text_buffer,40," ");
                    text_layer_set_text(login_lowertext_layer,text_buffer);
                    text_layer_set_font(login_lowertext_layer,fonts_get_system_font(strlen(text_buffer)>13?FONT_KEY_GOTHIC_24:FONT_KEY_GOTHIC_28));
                }
                user.logged_on = true;
                APP_LOG(APP_LOG_LEVEL_DEBUG, "Recieved user: %s | pts: %u | rank: %u | beac: %u | ach: %u | lev: %u | loc: %s",user.name,user.points,user.rank,user.beacons,user.achievements,user.level,user.location);
                if(window_stack_get_top_window()==achievements_window && user.achievements>num_achievements)
                    send_simple_request(REQUEST_ACHIEVEMENT_HEADERS);
            }
            else {
                //APP_LOG(APP_LOG_LEVEL_DEBUG, "Incorrect user dictionary");
            }
        }
        else if(*(receiving_type->value->data)==RESPONSE_BEACON && user.logged_on) {
            //APP_LOG(APP_LOG_LEVEL_DEBUG, "Starting receiving beacon");
            Tuple *id = dict_find(iter,BEACON_ID);
            Tuple *name = dict_find(iter,BEACON_NAME);
            Tuple *coworkers = dict_find(iter,BEACON_COWORKERS);
            Beacon *new_beacon = (Beacon*)malloc(sizeof(Beacon));
            if(new_beacon && coworkers && name && id) {
                //APP_LOG(APP_LOG_LEVEL_DEBUG, "strlen(name->value->cstring): %u",strlen(name->value->cstring));
                char new_name[strlen(name->value->cstring)+1];
                strncpy(new_name,name->value->cstring,sizeof(new_name));
                new_beacon->id = *(id->value->data);
                new_beacon->name = new_name;
                new_beacon->coworkers = *(coworkers->value->data);
                APP_LOG(APP_LOG_LEVEL_DEBUG, "Recieving beacon: %s | id: %i | coworkers: %u",new_beacon->name,new_beacon->id,new_beacon->coworkers);
                if(update_beacons_table(new_beacon)) {
                    if(window_stack_get_top_window()==beacons_window) {
                        //APP_LOG(APP_LOG_LEVEL_DEBUG, "Reloading beacons_menu_layer");
                        menu_layer_reload_data(beacons_menu_layer);
                    }
                }
            }
            else {
                //APP_LOG(APP_LOG_LEVEL_DEBUG, "Incorrect beacon dictionary");
            }
            if(num_beacons==user.beacons) {
                is_downloading = false;
                if(beacons_downloading_sign_layer)
                    layer_set_hidden(bitmap_layer_get_layer(beacons_downloading_sign_layer),true);
            }
        }
        else if(*(receiving_type->value->data)==RESPONSE_COWORKER && user.logged_on && current_beacon) {
            //APP_LOG(APP_LOG_LEVEL_DEBUG, "Starting receiving coworker");
            Tuple *id = dict_find(iter,COWORKER_ID);
            Tuple *name = dict_find(iter,COWORKER_NAME);
            Tuple *beacon_id = dict_find(iter,COWORKER_BEACON_ID);
            Coworker *new_coworker = (Coworker*)malloc(sizeof(Coworker));
            if(new_coworker && beacon_id && name && id) {
                char new_name[strlen(name->value->cstring)+1];
                strncpy(new_name,name->value->cstring,sizeof(new_name));
                new_coworker->id = *(id->value->data);
                new_coworker->name = new_name;
                new_coworker->beacon_id = *(beacon_id->value->data);
                APP_LOG(APP_LOG_LEVEL_DEBUG, "Recieved coworker: %s | beacon_id: %i",new_coworker->name,new_coworker->beacon_id);
                if(update_coworkers_table(new_coworker)) {
                    //APP_LOG(APP_LOG_LEVEL_DEBUG, "Reloading coworkers_menu_layer");
                    if(coworkers_menu_layer)
                        menu_layer_reload_data(coworkers_menu_layer);
                    if(num_coworkers==current_beacon->coworkers) {
                        is_downloading = false;
                        if(coworkers_downloading_sign_layer)
                            layer_set_hidden(bitmap_layer_get_layer(coworkers_downloading_sign_layer),true);
                    }
                    if(coworkers && window_stack_get_top_window()==coworkers_window) {
                        if(coworkers_text_layer)
                            layer_set_hidden(text_layer_get_layer(coworkers_text_layer),true);
                        if(coworkers_menu_layer)
                            layer_set_hidden(menu_layer_get_layer(coworkers_menu_layer),false);
                    }
                }
            }
            else {
                //APP_LOG(APP_LOG_LEVEL_DEBUG, "Incorrect coworker dictionary");
            }
        }
        else if(*(receiving_type->value->data)==RESPONSE_ACHIEVEMENT_HEADER && user.logged_on) {
            //APP_LOG(APP_LOG_LEVEL_DEBUG, "Starting receiving achievement header");
            Tuple *id = dict_find(iter,ACHIEVEMENT_ID);
            Tuple *name = dict_find(iter,ACHIEVEMENT_NAME);
            Tuple *parts = dict_find(iter,ACHIEVEMENT_NUMBER);
            Achievement *new_achievement = (Achievement*)malloc(sizeof(Achievement));
            if(new_achievement && parts && name && id) {
                char new_name[strlen(name->value->cstring)+1];
                strcpy(new_name,name->value->cstring);
                new_achievement->id = *(id->value->data);
                new_achievement->name = new_name;
                new_achievement->parts = *(parts->value->data);
                APP_LOG(APP_LOG_LEVEL_DEBUG, "Recieved achievement header: id: %i | name: %s | parts: %i",new_achievement->id,new_achievement->name,new_achievement->parts);
                if(update_achievements_table(new_achievement)) {
                    if(window_stack_get_top_window()==achievements_window) {
                        //APP_LOG(APP_LOG_LEVEL_DEBUG, "Reloading achievements_menu_layer");
                        menu_layer_reload_data(achievements_menu_layer);
                    }
                }
            }
            else {
                //APP_LOG(APP_LOG_LEVEL_DEBUG, "Incorrect achievement header dictionary");
            }
            if(num_achievements==user.achievements) {
                is_downloading = false;
                if(achievements_downloading_sign_layer) {
                    layer_set_hidden(bitmap_layer_get_layer(achievements_downloading_sign_layer),true);
                }
            }
        }
        
        else if(*(receiving_type->value->data)==RESPONSE_ACHIEVEMENT_CONTENT && user.logged_on && current_achievement) {
            //APP_LOG(APP_LOG_LEVEL_DEBUG, "Starting receiving achievement content");
            Tuple *id = dict_find(iter,ACHIEVEMENT_ID);
            Tuple *text = dict_find(iter,ACHIEVEMENT_NAME);
            Tuple *part = dict_find(iter,ACHIEVEMENT_NUMBER);
            if(part && text && id) {
                if(current_achievement->id==*(id->value->data)) {
                    if(*(part->value->data)==0)
                        strcpy(current_achievement_description,text->value->cstring);
                    else
                        strcat(current_achievement_description,text->value->cstring);
                }
                APP_LOG(APP_LOG_LEVEL_DEBUG, "Recieved achievement description: id: %i | part: %i | text: %s",*(id->value->data),*(part->value->data),text->value->cstring);
                text_layer_set_text(achievement_details_content_text_layer,current_achievement_description);
                GSize max_content_size = text_layer_get_content_size(achievement_details_content_text_layer);
                max_content_size.w = 134;
                max_content_size.h += 10;
                text_layer_set_size(achievement_details_content_text_layer,max_content_size);
                scroll_layer_set_content_size(achievement_details_scroll_layer,GSize(144,max_content_size.h));
                if(*(part->value->data)==current_achievement->parts-1)
                    is_downloading = false;
                if(achievement_details_downloading_sign_layer)
                    layer_set_hidden(bitmap_layer_get_layer(achievement_details_downloading_sign_layer),true);
            }
            else {
                //APP_LOG(APP_LOG_LEVEL_DEBUG, "Incorrect achievement content dictionary");
            }
        }
        else if(*(receiving_type->value->data)==RESPONSE_COWORKER_POP && user.logged_on) {
            //APP_LOG(APP_LOG_LEVEL_DEBUG, "Starting receiving coworker to pop");
            Tuple *id = dict_find(iter,COWORKER_ID);
            Tuple *name = dict_find(iter,COWORKER_NAME);
            Tuple *beacon_id = dict_find(iter,COWORKER_BEACON_ID);
            if(beacon_id && id) {
                APP_LOG(APP_LOG_LEVEL_DEBUG, "Recieved coworker to pop: id: %i | name: %s | from room id: %i",*(id->value->data),name->value->cstring,*(beacon_id->value->data));
                if(*(beacon_id->value->data)==current_beacon->id && pop_coworker_from_table(*(id->value->data))) {
                    if(window_stack_get_top_window()==coworkers_window) {
                        //APP_LOG(APP_LOG_LEVEL_DEBUG, "Reloading coworkers_menu_layer");
                        menu_layer_reload_data(coworkers_menu_layer);
                    }
                }
                is_downloading = false;
                if(coworkers_downloading_sign_layer)
                    layer_set_hidden(bitmap_layer_get_layer(coworkers_downloading_sign_layer),true);
                if(coworkers==NULL) {
                    if(coworkers_menu_layer)
                        layer_set_hidden(menu_layer_get_layer(coworkers_menu_layer),true);
                    if(coworkers_text_layer)
                        layer_set_hidden(text_layer_get_layer(coworkers_text_layer),false);
                }
            }
            else {
                //APP_LOG(APP_LOG_LEVEL_DEBUG, "Incorrect coworker pop dictionary");
            }
        }
    }
    else {
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "Receiving error, RESPONSE_TYPE tuple not found");
    }
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "in_received_handler() end");
}

void in_dropped_handler(AppMessageResult reason, void *context) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "Receiving rejected. Reason: %s",translate_result(reason));
}
/////////////////////////////////////

static int16_t get_header_height(MenuLayer *menu_layer, uint16_t section_index, void *data) {
    return MENU_CELL_BASIC_HEADER_HEIGHT;
}

static int16_t get_cell_height(MenuLayer *menu_layer, MenuIndex *cell_index, void *data) {
    return 22;
}

///////////////////////////////////// LOGIN WINDOW
static AppTimer *login_timer;
static SimpleMenuSection login_menu_sections[1];
static SimpleMenuItem login_menu_first_section_items[2];

void login_animation_started(Animation *animation, void *data) {
    layer_set_hidden(text_layer_get_layer(login_lowertext_layer),true);
}

void login_animation_stopped(Animation *animation, void *data) {
    GRect lowertext_bounds = layer_get_frame(text_layer_get_layer(login_lowertext_layer));
    lowertext_bounds.origin.y = 32;
    static char text_buffer[50];
    snprintf(text_buffer,50,"in %s",user.location);
    text_layer_set_text(login_lowertext_layer,text_buffer);
    text_layer_set_font(login_lowertext_layer,fonts_get_system_font(strlen(text_buffer)>13?FONT_KEY_GOTHIC_24:FONT_KEY_GOTHIC_28));
    layer_set_frame(text_layer_get_layer(login_lowertext_layer),lowertext_bounds);
    layer_set_hidden(text_layer_get_layer(login_lowertext_layer),false);
    static char text_buffer2[30];
    snprintf(text_buffer2,30,"%s",user.name);
    login_menu_first_section_items[1].subtitle = text_buffer2;
    layer_set_hidden(simple_menu_layer_get_layer(login_menu_layer),false);
}

static void fire_login_animation() {
    GRect from_frame = layer_get_frame(text_layer_get_layer(login_uppertext_layer));
    GRect to_frame = GRect(0,0,from_frame.size.w,from_frame.size.h);
    login_property_animation = property_animation_create_layer_frame(text_layer_get_layer(login_uppertext_layer), &from_frame, &to_frame);
    animation_set_handlers((Animation*)login_property_animation, (AnimationHandlers) {
        .started = (AnimationStartedHandler)login_animation_started,
        .stopped = (AnimationStoppedHandler)login_animation_stopped,
      }, NULL);
    animation_schedule((Animation*)login_property_animation);
}

static void login_window_bt_handler(bool connected) {
    if(login_lowertext_layer) {
        if(connected)
            text_layer_set_text(login_lowertext_layer,"sign in on your smartphone");
        else
            text_layer_set_text(login_lowertext_layer,"connect your smartphone");
    }
}

static void login_menu_beacons_callback(int index, void *ctx) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "login_menu_beacons_callback()");
    if(user.name) {
        if(user.beacons!=num_beacons) {
            send_simple_request(REQUEST_BEACONS);
            is_downloading = true;
        }
        window_stack_push(beacons_window,animated);
    }
}

static void login_menu_user_callback(int index, void *ctx) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "login_menu_user_callback()");
    if(user.name) {
        window_stack_push(user_window,animated);
    }
}

static void login_request_sending(void *data) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "login_request_sending()");
    if(user.name==NULL) {
        send_simple_request(REQUEST_USER);
        login_timer = app_timer_register(2000,login_request_sending,NULL);
    }
}

static void login_window_load(Window *window) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "login_window_load() start");
    login_timer = app_timer_register(2000,login_request_sending,NULL);
    send_simple_request(REQUEST_USER);
    
    Layer *window_layer = window_get_root_layer(window);
    int uppertext_height = 26;
    int lowertext_height = 53;
    
    GRect uppertext_bounds = layer_get_bounds(window_layer);
    uppertext_bounds.origin.y += 50;
    uppertext_bounds.size.h = uppertext_height;
    login_uppertext_layer = text_layer_create(uppertext_bounds);
    text_layer_set_text(login_uppertext_layer,"Get Results!");
    text_layer_set_font(login_uppertext_layer,fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD));
    text_layer_set_text_alignment(login_uppertext_layer, GTextAlignmentCenter);
    
    GRect lowertext_bounds = layer_get_bounds(window_layer);
    lowertext_bounds.size.h = lowertext_height;
    lowertext_bounds.origin.y += uppertext_height + 50;
    login_lowertext_layer = text_layer_create(lowertext_bounds);
    if(bluetooth_connection_service_peek())
        text_layer_set_text(login_lowertext_layer,"sign in on your smartphone");
    else
        text_layer_set_text(login_lowertext_layer,"connect your smartphone");
    text_layer_set_font(login_lowertext_layer,fonts_get_system_font(FONT_KEY_GOTHIC_24));
    text_layer_set_text_alignment(login_lowertext_layer, GTextAlignmentCenter);
    
    login_menu_first_section_items[0] = (SimpleMenuItem) {
        .title = "Locations",
        .callback = login_menu_beacons_callback,
        .icon = beacons_icon
    };
    login_menu_first_section_items[1] = (SimpleMenuItem) {
        .title = "User details",
        .subtitle = "username",
        .callback = login_menu_user_callback,
        .icon = user_icon
    };
    
    login_menu_sections[0] = (SimpleMenuSection) {
        .num_items = 2,
        .items = login_menu_first_section_items
    };
    
    GRect menu_bounds = layer_get_bounds(window_layer);
    menu_bounds.size.h -= uppertext_height + lowertext_height;
    menu_bounds.origin.y += uppertext_height + lowertext_height;
    login_menu_layer = simple_menu_layer_create(menu_bounds,window,login_menu_sections,1,NULL);
    
    layer_add_child(window_layer, text_layer_get_layer(login_uppertext_layer));
    layer_add_child(window_layer, text_layer_get_layer(login_lowertext_layer));
    layer_add_child(window_layer, simple_menu_layer_get_layer(login_menu_layer));
    
    if(user.name==NULL)
        layer_set_hidden(simple_menu_layer_get_layer(login_menu_layer),true);
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "login_window_load() end");
}

static void login_window_unload(Window *window) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "login_window_unload() start");
    if(animation_is_scheduled((Animation*)login_property_animation))
        animation_unschedule((Animation*)login_property_animation);
    property_animation_destroy(login_property_animation);
    login_property_animation = NULL;
    
    simple_menu_layer_destroy(login_menu_layer);
    login_menu_layer = NULL;
    text_layer_destroy(login_lowertext_layer);
    login_lowertext_layer = NULL;
    text_layer_destroy(login_uppertext_layer);
    login_uppertext_layer = NULL;
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "login_window_unload() end");
}
/////////////////////////////////////

///////////////////////////////////// USER WINDOW
static SimpleMenuSection user_menu_sections[1];
static SimpleMenuItem user_menu_first_section_items[1];

static void user_menu_achievements_callback(int index, void *ctx) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "user_menu_achievements_callback()");
    if(!achievements_first_time || user.achievements>num_achievements) {
        send_simple_request(REQUEST_ACHIEVEMENT_HEADERS);
        is_downloading = true;
        achievements_first_time = true;
    }
    window_stack_push(achievements_window,animated);
}

static void user_window_load(Window *window) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "user_window_load() start");
    Layer *window_layer = window_get_root_layer(window);
    int text_layer_height = 100;
    int left_text_layer_width = 114;
    int right_text_layer_width = 45;
    if(user.points>9999)
        right_text_layer_width = 55;
    else if(user.points>99999)
        right_text_layer_width = 65;
    
    set_textbar_layer(window_layer,&user_textbar_layer);
    text_layer_set_text(user_textbar_layer,user.name);
    
    GRect left_text_bounds = layer_get_bounds(window_layer);
    left_text_bounds.size.h = text_layer_height;
    left_text_bounds.size.w = left_text_layer_width;
    left_text_bounds.origin.y += textbar_height;
    user_left_text_layer = text_layer_create(left_text_bounds);
    static char text_buffer1[40];
    snprintf(text_buffer1,40," Level:\n Points:\n Rank:\n Achievements:");
    text_layer_set_text(user_left_text_layer,text_buffer1);
    text_layer_set_font(user_left_text_layer,fonts_get_system_font(FONT_KEY_GOTHIC_24));
    text_layer_set_text_alignment(user_left_text_layer, GTextAlignmentLeft);
    
    GRect right_text_bounds = left_text_bounds;
    right_text_bounds.size.w = right_text_layer_width;
    right_text_bounds.origin.x = 144-right_text_layer_width;
    user_right_text_layer = text_layer_create(right_text_bounds);
    static char text_buffer2[25];
    snprintf(text_buffer2,25,"%i \n%i \n%i \n%i ",user.level,user.points,user.rank,user.achievements);
    text_layer_set_text(user_right_text_layer,text_buffer2);
    text_layer_set_font(user_right_text_layer,fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD));
    text_layer_set_text_alignment(user_right_text_layer, GTextAlignmentRight);
    
    user_menu_first_section_items[0] = (SimpleMenuItem) {
        .title = "Achievements",
        .callback = user_menu_achievements_callback,
        .icon = achievements_icon
    };
    
    user_menu_sections[0] = (SimpleMenuSection) {
        .num_items = 1,
        .items = user_menu_first_section_items
    };
    
    GRect menu_bounds = layer_get_bounds(window_layer);
    menu_bounds.size.h -= textbar_height + text_layer_height;
    menu_bounds.origin.y += textbar_height + text_layer_height;
    user_menu_layer = simple_menu_layer_create(menu_bounds,window,user_menu_sections,1,NULL);
    
    layer_add_child(window_layer, text_layer_get_layer(user_textbar_layer));
    layer_add_child(window_layer, text_layer_get_layer(user_left_text_layer));
    layer_add_child(window_layer, text_layer_get_layer(user_right_text_layer));
    layer_add_child(window_layer, simple_menu_layer_get_layer(user_menu_layer));
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "user_window_load() end");
}

static void user_window_unload(Window *window) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "user_window_unload() start");
    simple_menu_layer_destroy(user_menu_layer);
    user_menu_layer = NULL;
    text_layer_destroy(user_right_text_layer);
    user_right_text_layer = NULL;
    text_layer_destroy(user_left_text_layer);
    user_left_text_layer = NULL;
    text_layer_destroy(user_textbar_layer);
    user_textbar_layer = NULL;
    
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "user_window_unload() end");
}
/////////////////////////////////////

///////////////////////////////////// BEACONS WINDOW
static uint16_t get_num_sections_beacons(MenuLayer *menu_layer, void *data) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "get_num_sections_beacons()");
    //return 2;
    return 1;
}

static void draw_beacon_header(GContext* ctx, const Layer *cell_layer, uint16_t section_index, void *data) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "draw_beacon_header()");
    if(beacons) {
        switch (section_index) {
            case 0: {
                GRect header_layer_bounds = layer_get_bounds(cell_layer);
                header_layer_bounds.origin.y -= 1;
                graphics_context_set_text_color(ctx, GColorBlack);
                graphics_draw_text(ctx, "Locations", fonts_get_system_font(FONT_KEY_GOTHIC_14_BOLD), header_layer_bounds, GTextOverflowModeFill , GTextAlignmentCenter, NULL);
                break;
            }
        }
    }
    else
        menu_cell_basic_header_draw(ctx, cell_layer, "Downloading locations...");
}

static void draw_beacon_row(GContext *ctx, const Layer *cell_layer, MenuIndex *cell_index, void *callback_context) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "draw_beacon_row()");
    switch (cell_index->section) {
        case 0:
            if(beacons) {
                GRect cell_layer_bounds = layer_get_bounds(cell_layer);
                GRect beacon_name_bounds = cell_layer_bounds;
                beacon_name_bounds.size.w -= 20;
                beacon_name_bounds.origin.x += 5;
                beacon_name_bounds.origin.y -= 2;
                GRect beacon_coworkers_bounds = cell_layer_bounds;
                beacon_coworkers_bounds.size.w = 25;
                beacon_coworkers_bounds.origin.x = beacon_name_bounds.size.w;
                beacon_coworkers_bounds.origin.y -= 2;
                static char text_buffer1[4];
                snprintf(text_buffer1,4,"%i",beacons[cell_index->row]->coworkers);
                graphics_context_set_text_color(ctx, GColorBlack);
                graphics_draw_text(ctx, beacons[cell_index->row]->name, fonts_get_system_font(FONT_KEY_GOTHIC_18), beacon_name_bounds, GTextOverflowModeFill , GTextAlignmentLeft, NULL);
                graphics_draw_text(ctx, text_buffer1, fonts_get_system_font(FONT_KEY_GOTHIC_18), beacon_coworkers_bounds, GTextOverflowModeWordWrap , GTextAlignmentCenter, NULL);
            }
            break;
    }
}

static void beacon_select_click(struct MenuLayer *menu_layer, MenuIndex *cell_index, void *callback_context) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "beacon_select_click()");
    if(cell_index->section==0 && beacons) {
        current_beacon = beacons[cell_index->row];
    }
    
    if(current_beacon) {
        if(previous_beacon!=current_beacon) {
            clear_coworkers_table();
            send_query_request(REQUEST_COWORKERS,current_beacon->id);
            is_downloading = true;
        }
        previous_beacon = current_beacon;
        window_stack_push(coworkers_window,animated);
    }
}

MenuLayerCallbacks beacons_menu_callbacks = {
    .get_num_sections = get_num_sections_beacons,
    .get_num_rows = get_num_beacons,
    .get_header_height = get_header_height,
    .get_cell_height = get_cell_height,
    .draw_header = draw_beacon_header,
    .draw_row = draw_beacon_row,
    .select_click = beacon_select_click
};

static void beacons_window_load(Window *window) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "beacons_window_load() start");
    Layer *window_layer = window_get_root_layer(window);
    
    set_textbar_layer(window_layer,&beacons_textbar_layer);
    text_layer_set_text(beacons_textbar_layer,user.name);
    
    set_downloading_sign_layer(window_layer,&beacons_downloading_sign_layer);
    layer_set_hidden(bitmap_layer_get_layer(beacons_downloading_sign_layer),(is_downloading && last_request==REQUEST_BEACONS)?false:true);
    
    GRect menu_bounds = layer_get_bounds(window_layer);
    menu_bounds.size.h -= textbar_height;
    menu_bounds.origin.y += textbar_height;
    beacons_menu_layer = menu_layer_create(menu_bounds);
    menu_layer_set_callbacks(beacons_menu_layer, NULL, beacons_menu_callbacks);
    menu_layer_set_click_config_onto_window(beacons_menu_layer, window);
    
    layer_add_child(window_layer, text_layer_get_layer(beacons_textbar_layer));
    layer_add_child(window_layer, bitmap_layer_get_layer(beacons_downloading_sign_layer));
    layer_add_child(window_layer, menu_layer_get_layer(beacons_menu_layer));
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "beacons_window_load() end");
}

static void beacons_window_unload(Window *window) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "beacons_window_unload() start");
    menu_layer_destroy(beacons_menu_layer);
    beacons_menu_layer = NULL;
    bitmap_layer_destroy(beacons_downloading_sign_layer);
    beacons_downloading_sign_layer = NULL;
    text_layer_destroy(beacons_textbar_layer);
    beacons_textbar_layer = NULL;
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "beacons_window_unload() start");
}
/////////////////////////////////////

///////////////////////////////////// COWORKERS WINDOW
static uint16_t get_num_sections_coworkers(MenuLayer *menu_layer, void *data) {
    return 1;
}

static void draw_coworker_header(GContext* ctx, const Layer *cell_layer, uint16_t section_index, void *data) {
    if(coworkers) {
        switch (section_index) {
            case 0: {
                GRect header_layer_bounds = layer_get_bounds(cell_layer);
                header_layer_bounds.origin.y -= 1;
                graphics_context_set_text_color(ctx, GColorBlack);
                graphics_draw_text(ctx, "Coworkers in location", fonts_get_system_font(FONT_KEY_GOTHIC_14_BOLD), header_layer_bounds, GTextOverflowModeFill , GTextAlignmentCenter, NULL);
                break;
            }
        }
    }
    else
        menu_cell_basic_header_draw(ctx, cell_layer, "This room is empty");
}

static void draw_coworker_row(GContext *ctx, const Layer *cell_layer, MenuIndex *cell_index, void *callback_context) {
    if(coworkers) {
        switch (cell_index->section) {
            case 0:
                if(coworkers && coworkers[cell_index->row]->name) {
                    GRect cell_layer_bounds = layer_get_bounds(cell_layer);
                    cell_layer_bounds.size.w -= 10;
                    cell_layer_bounds.origin.x += 5;
                    cell_layer_bounds.origin.y -= 2;
                    graphics_context_set_text_color(ctx, GColorBlack);
                    graphics_draw_text(ctx, coworkers[cell_index->row]->name, fonts_get_system_font(FONT_KEY_GOTHIC_18), cell_layer_bounds, GTextOverflowModeFill , GTextAlignmentLeft, NULL);
                }
                break;
        }
    }
}

static void coworker_select_click(struct MenuLayer *menu_layer, MenuIndex *cell_index, void *callback_context) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "coworker_select_click() start");
    /*
     if(cell_index->section==0) {
     current_coworker = coworkers[cell_index->row];
     }
     
     if(current_coworker)
     //APP_LOG(APP_LOG_LEVEL_DEBUG, "coworker selected: %s",current_coworker->name);
     else
     //APP_LOG(APP_LOG_LEVEL_DEBUG, "current_coworker==NULL!");
     
     window_stack_push(coworker_details_window,animated); // or maybe user_window???
     */
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "coworker_select_click() end");
}

MenuLayerCallbacks coworkers_menu_callbacks = {
    .get_num_sections = get_num_sections_coworkers,
    .get_num_rows = get_num_coworkers,
    .get_header_height = get_header_height,
    .get_cell_height = get_cell_height,
    .draw_header = draw_coworker_header,
    .draw_row = draw_coworker_row,
    .select_click = coworker_select_click
};

static void coworkers_window_load(Window *window) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "coworkers_window_load() start");
    Layer *window_layer = window_get_root_layer(window);
    
    set_textbar_layer(window_layer,&coworkers_textbar_layer);
    text_layer_set_text(coworkers_textbar_layer,current_beacon->name);
    
    set_downloading_sign_layer(window_layer,&coworkers_downloading_sign_layer);
    layer_set_hidden(bitmap_layer_get_layer(coworkers_downloading_sign_layer),(is_downloading && last_request==REQUEST_COWORKERS && current_beacon->coworkers>0)?false:true);
    
    GRect menu_bounds = layer_get_bounds(window_layer);
    menu_bounds.size.h -= textbar_height;
    menu_bounds.origin.y += textbar_height;
    coworkers_menu_layer = menu_layer_create(menu_bounds);
    menu_layer_set_callbacks(coworkers_menu_layer, NULL, coworkers_menu_callbacks);
    menu_layer_set_click_config_onto_window(coworkers_menu_layer, window);
    
    coworkers_text_layer = text_layer_create(menu_bounds);
    text_layer_set_text(coworkers_text_layer,"This room is empty");
    text_layer_set_font(coworkers_text_layer,fonts_get_system_font(FONT_KEY_GOTHIC_18));
    text_layer_set_text_alignment(coworkers_text_layer, GTextAlignmentCenter);
    
    if(current_beacon->coworkers!=0) {
        layer_set_hidden(text_layer_get_layer(coworkers_text_layer),true);
        layer_set_hidden(menu_layer_get_layer(coworkers_menu_layer),false);
    }
    else {
        layer_set_hidden(menu_layer_get_layer(coworkers_menu_layer),true);
        layer_set_hidden(text_layer_get_layer(coworkers_text_layer),false);
    }
    
    layer_add_child(window_layer, text_layer_get_layer(coworkers_textbar_layer));
    layer_add_child(window_layer, bitmap_layer_get_layer(coworkers_downloading_sign_layer));
    layer_add_child(window_layer, menu_layer_get_layer(coworkers_menu_layer));
    layer_add_child(window_layer, text_layer_get_layer(coworkers_text_layer));
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "coworkers_window_load() end");
}

static void coworkers_window_unload(Window *window) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "coworkers_window_unload() start");
    text_layer_destroy(coworkers_text_layer);
    coworkers_text_layer = NULL;
    menu_layer_destroy(coworkers_menu_layer);
    coworkers_menu_layer = NULL;
    bitmap_layer_destroy(coworkers_downloading_sign_layer);
    coworkers_downloading_sign_layer = NULL;
    text_layer_destroy(coworkers_textbar_layer);
    coworkers_textbar_layer = NULL;
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "coworkers_window_unload() end");
}
/////////////////////////////////////

///////////////////////////////////// ACHIEVEMENTS WINDOW
static uint16_t get_num_sections_achievements(MenuLayer *menu_layer, void *data) {
    if(achievements)
        return 1;
    else
        return 0;
}

static void draw_achievement_header(GContext* ctx, const Layer *cell_layer, uint16_t section_index, void *data) {
    if(achievements) {
        switch (section_index) {
            case 0: {
                GRect header_layer_bounds = layer_get_bounds(cell_layer);
                header_layer_bounds.origin.y -= 1;
                graphics_context_set_text_color(ctx, GColorBlack);
                graphics_draw_text(ctx, "Achievements", fonts_get_system_font(FONT_KEY_GOTHIC_14_BOLD), header_layer_bounds, GTextOverflowModeFill , GTextAlignmentCenter, NULL);
                break;
            }
        }
    }
    else
        menu_cell_basic_header_draw(ctx, cell_layer, "You haven't gained any achievement");
}

static void draw_achievement_row(GContext *ctx, const Layer *cell_layer, MenuIndex *cell_index, void *callback_context) {
    if(achievements) {
        switch (cell_index->section) {
            case 0:
                if(achievements[cell_index->row]->name) {
                    GRect cell_layer_bounds = layer_get_bounds(cell_layer);
                    cell_layer_bounds.size.w -= 10;
                    cell_layer_bounds.origin.x += 5;
                    cell_layer_bounds.origin.y -= 2;
                    graphics_context_set_text_color(ctx, GColorBlack);
                    graphics_draw_text(ctx, achievements[cell_index->row]->name, fonts_get_system_font(FONT_KEY_GOTHIC_18), cell_layer_bounds, GTextOverflowModeFill , GTextAlignmentLeft, NULL);
                }
                break;
        }
    }
    else
        menu_cell_basic_draw(ctx, cell_layer, "NOTHING...", NULL, NULL);
}

static void achievement_select_click(struct MenuLayer *menu_layer, MenuIndex *cell_index, void *callback_context) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "achievement_select_click()");
    if(cell_index->section==0 && achievements)
        current_achievement = achievements[cell_index->row];
    if(current_achievement) {
        if(previous_achievement!=current_achievement) {
            current_achievement_description[0] = '\0';
            send_query_request(REQUEST_ACHIEVEMENT_CONTENT,current_achievement->id);
            is_downloading = true;
        }
 		previous_achievement = current_achievement;
 		window_stack_push(achievement_details_window,animated);
    }
}

MenuLayerCallbacks achievements_menu_callbacks = {
    .get_num_sections = get_num_sections_achievements,
    .get_num_rows = get_num_achievements,
    .get_header_height = get_header_height,
    .get_cell_height = get_cell_height,
    .draw_header = draw_achievement_header,
    .draw_row = draw_achievement_row,
    .select_click = achievement_select_click
};

static void achievements_window_load(Window *window) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "achievements_window_load() start");
    Layer *window_layer = window_get_root_layer(window);
    
    set_textbar_layer(window_layer,&achievements_textbar_layer);
    text_layer_set_text(achievements_textbar_layer,user.name);
    
    set_downloading_sign_layer(window_layer,&achievements_downloading_sign_layer);
    layer_set_hidden(bitmap_layer_get_layer(achievements_downloading_sign_layer),(is_downloading && user.achievements!=num_achievements && last_request==REQUEST_ACHIEVEMENT_HEADERS)?false:true);
    
    GRect menu_bounds = layer_get_bounds(window_layer);
    menu_bounds.size.h -= textbar_height;
    menu_bounds.origin.y += textbar_height;
    achievements_menu_layer = menu_layer_create(menu_bounds);
    menu_layer_set_callbacks(achievements_menu_layer, NULL, achievements_menu_callbacks);
    menu_layer_set_click_config_onto_window(achievements_menu_layer, window);
    
    layer_add_child(window_layer, text_layer_get_layer(achievements_textbar_layer));
    layer_add_child(window_layer, bitmap_layer_get_layer(achievements_downloading_sign_layer));
    layer_add_child(window_layer, menu_layer_get_layer(achievements_menu_layer));
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "achievements_window_load() end");
}

static void achievements_window_unload(Window *window) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "achievements_window_unload() start");
    menu_layer_destroy(achievements_menu_layer);
    achievements_menu_layer = NULL;
    bitmap_layer_destroy(achievements_downloading_sign_layer);
    achievements_downloading_sign_layer = NULL;
    text_layer_destroy(achievements_textbar_layer);
    achievements_textbar_layer = NULL;
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "achievements_window_unload() end");
}
/////////////////////////////////////

///////////////////////////////////// ACHIEVEMENT DETAILS WINDOW
static void achievement_details_window_load(Window *window) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "achievement_details_window_load() start");
    Layer *window_layer = window_get_root_layer(window);
    
    set_textbar_layer(window_layer,&achievement_details_textbar_layer);
    text_layer_set_text(achievement_details_textbar_layer,user.name);
    
    set_downloading_sign_layer(window_layer,&achievement_details_downloading_sign_layer);
    layer_set_hidden(bitmap_layer_get_layer(achievement_details_downloading_sign_layer),(is_downloading && last_request==REQUEST_ACHIEVEMENT_CONTENT)?false:true);
    
    GRect max_text_bounds = GRect(5,0,layer_get_bounds(window_layer).size.w-10,500);    
    achievement_details_title_text_layer = text_layer_create(max_text_bounds);
    text_layer_set_text(achievement_details_title_text_layer,current_achievement->name);
    text_layer_set_font(achievement_details_title_text_layer,fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD));
    text_layer_set_text_alignment(achievement_details_title_text_layer, GTextAlignmentCenter);
    GSize max_title_size = text_layer_get_content_size(achievement_details_title_text_layer);
    max_title_size.w = max_text_bounds.size.w;
    text_layer_set_size(achievement_details_title_text_layer,max_title_size);
    
    max_text_bounds.origin.y += max_title_size.h;
    achievement_details_content_text_layer = text_layer_create(max_text_bounds);
    text_layer_set_text(achievement_details_content_text_layer,current_achievement_description);
    text_layer_set_font(achievement_details_content_text_layer,fonts_get_system_font(FONT_KEY_GOTHIC_24));
    text_layer_set_text_alignment(achievement_details_content_text_layer, GTextAlignmentCenter);
    GSize max_content_size = text_layer_get_content_size(achievement_details_content_text_layer);
    max_content_size.w = max_text_bounds.size.w;
    max_content_size.h += 10;
    text_layer_set_size(achievement_details_content_text_layer,max_content_size);
    
    GRect scroll_layer_bounds = layer_get_bounds(window_layer);
    scroll_layer_bounds.size.h -= textbar_height;
    scroll_layer_bounds.origin.y = textbar_height;
    achievement_details_scroll_layer = scroll_layer_create(scroll_layer_bounds);
    scroll_layer_set_click_config_onto_window(achievement_details_scroll_layer,achievement_details_window);
    
    scroll_layer_set_content_size(achievement_details_scroll_layer,GSize(max_text_bounds.size.w,max_title_size.h+max_content_size.h));
    scroll_layer_add_child(achievement_details_scroll_layer,text_layer_get_layer(achievement_details_title_text_layer));
    scroll_layer_add_child(achievement_details_scroll_layer,text_layer_get_layer(achievement_details_content_text_layer));
    
    layer_add_child(window_layer, text_layer_get_layer(achievement_details_textbar_layer));
    layer_add_child(window_layer, bitmap_layer_get_layer(achievement_details_downloading_sign_layer));
    layer_add_child(window_layer, scroll_layer_get_layer(achievement_details_scroll_layer));
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "achievement_details_window_load() end");
}

static void achievement_details_window_unload(Window *window) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "achievement_details_window_unload() start");
    text_layer_destroy(achievement_details_content_text_layer);
    achievement_details_content_text_layer = NULL;
    text_layer_destroy(achievement_details_title_text_layer);
    achievement_details_title_text_layer = NULL;
    scroll_layer_destroy(achievement_details_scroll_layer);
    achievement_details_scroll_layer = NULL;
    bitmap_layer_destroy(achievement_details_downloading_sign_layer);
    achievement_details_downloading_sign_layer = NULL;
    text_layer_destroy(achievement_details_textbar_layer);
    achievement_details_textbar_layer = NULL;
    //current_achievement = NULL;
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "achievement_details_window_unload() end");
}
/////////////////////////////////////

/////////////////////////////////////
static WindowHandlers login_window_handlers = {
    .load = login_window_load,
    .unload = login_window_unload
};
static WindowHandlers user_window_handlers = {
    .load = user_window_load,
    .unload = user_window_unload
};
static WindowHandlers beacons_window_handlers = {
    .load = beacons_window_load,
    .unload = beacons_window_unload
};
static WindowHandlers coworkers_window_handlers = {
    .load = coworkers_window_load,
    .unload = coworkers_window_unload
};
static WindowHandlers achievements_window_handlers = {
    .load = achievements_window_load,
    .unload = achievements_window_unload
};
static WindowHandlers achievement_details_window_handlers = {
    .load = achievement_details_window_load,
    .unload = achievement_details_window_unload
};

static void init() {
    num_beacons = 0;
    num_coworkers = 0;
    num_achievements = 0;
    last_request = 0;
    
    user.logged_on = false;
    
    is_downloading = false;
    achievements_first_time = false;
    
    beacons_icon = gbitmap_create_with_resource(RESOURCE_ID_ICON_BEACON);
    user_icon = gbitmap_create_with_resource(RESOURCE_ID_ICON_USER);
    achievements_icon = gbitmap_create_with_resource(RESOURCE_ID_ICON_ACHIEVEMENT);
    downloading_sign_image = gbitmap_create_with_resource(RESOURCE_ID_ICON_DOWNLOADING);
    
    login_window = window_create();
    window_set_fullscreen(login_window, true);
    window_set_window_handlers(login_window, login_window_handlers);
    
    user_window = window_create();
    window_set_fullscreen(user_window, true);
    window_set_window_handlers(user_window, user_window_handlers);
    
    beacons_window = window_create();
    window_set_fullscreen(beacons_window, true);
    window_set_window_handlers(beacons_window, beacons_window_handlers);
    
    coworkers_window = window_create();
    window_set_fullscreen(coworkers_window, true);
    window_set_window_handlers(coworkers_window, coworkers_window_handlers);
    
    achievements_window = window_create();
    window_set_fullscreen(achievements_window, true);
    window_set_window_handlers(achievements_window, achievements_window_handlers);
    
    achievement_details_window = window_create();
    window_set_fullscreen(achievement_details_window, true);
    window_set_window_handlers(achievement_details_window, achievement_details_window_handlers);
    
    app_message_register_inbox_received(in_received_handler);
    app_message_register_inbox_dropped(in_dropped_handler);
    app_message_register_outbox_sent(out_sent_handler);
    app_message_register_outbox_failed(out_failed_handler);
    
    const int inbound_size = app_message_inbox_size_maximum();
    const int outbound_size = 128;
    app_message_open(inbound_size,outbound_size);
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "App_message initialising: %s",translate_result(app_message_open(200,outbound_size)));
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "App_message inbound size: %u",inbound_size);
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "App_message outbound size: %u",outbound_size);
    app_comm_set_sniff_interval(SNIFF_INTERVAL_REDUCED);
    
    bluetooth_connection_service_subscribe(login_window_bt_handler);
    
    window_stack_push(login_window,animated);
}

static void deinit() {
    window_destroy(achievement_details_window);
    window_destroy(achievements_window);
    window_destroy(coworkers_window);
    window_destroy(beacons_window);
    window_destroy(user_window);
    window_destroy(login_window);
    
    gbitmap_destroy(downloading_sign_image);
    gbitmap_destroy(achievements_icon);
    gbitmap_destroy(beacons_icon);
    gbitmap_destroy(user_icon);
    
    if(user.name) {
        free(user.name);
        user.name = NULL;
    }
    if(user.location) {
        free(user.location);
        user.location = NULL;
    }
    clear_beacons_table();
    clear_coworkers_table();
    clear_achievements_table();
}
/////////////////////////////////////

int main(void) {
    init();
    app_event_loop();
    deinit();
}