///    @author: Pawel Palka
///    @email: ppalka@sosoftware.pl    // company
///    @email: fractalord@gmail.com    // private

#include "pebble.h"

static Window *login_window;
static SimpleMenuLayer *login_menu_layer;
static TextLayer *login_uppertext_layer;
static TextLayer *login_lowertext_layer;

static Window *user_window;
static SimpleMenuLayer *user_menu_layer;
static TextLayer *user_textbar_layer;
static TextLayer *user_text_layer;
static Layer *user_downloading_sign_layer;

static Window *beacons_window;
static MenuLayer *beacons_menu_layer;
static TextLayer *beacons_textbar_layer ;
static Layer *beacons_downloading_sign_layer;

static Window *coworkers_window;
static MenuLayer *coworkers_menu_layer;
static TextLayer *coworkers_textbar_layer;
static TextLayer *coworkers_text_layer;
static Layer *coworkers_downloading_sign_layer;

static Window *achievements_window;
static MenuLayer *achievements_menu_layer;
static TextLayer *achievements_textbar_layer;
static Layer *achievements_downloading_sign_layer;

static Window *achievement_details_window;
static TextLayer *achievement_details_textbar_layer;
static TextLayer *achievement_details_text_layer;
static ScrollLayer *achievement_details_scroll_layer;

typedef struct {
    char *name;
    char *location;
    int points;
    int rank;
    int beacons;
} User;

typedef struct {
    int id;
    char *name;
    int coworkers;
} Beacon;

typedef struct {
    int id;
    char *name;
} Coworker;

typedef struct {
    int id;
    char *name;
    char *description;
} Achievement;

static Beacon *current_beacon;
static Beacon *previous_beacon;
static Achievement *current_achievement;

static User user;
static Beacon **beacons;
static Coworker **coworkers;
static Achievement **achievements;

uint16_t num_beacons;
uint16_t num_beacons_in_range;
uint16_t num_beacons_out_of_range;
uint16_t num_coworkers;
uint16_t max_coworkers;
uint16_t num_achievements;
uint16_t max_achievements;

uint8_t last_request;

static bool is_downloading;

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
        beacons = (Beacon**)calloc(user.beacons,sizeof(Beacon*));
        char *new_name = (char*)calloc(strlen(new_beacon->name),sizeof(char));
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
                beacons[i]->coworkers=new_beacon->coworkers;
                //APP_LOG(APP_LOG_LEVEL_DEBUG, "    beacon found, rejecting");
                free(new_beacon);
                new_beacon = NULL;
                return false;
            }
        }
        // add new if not found in table
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "    beacon not found, adding new");
        char *new_name = (char*)calloc(strlen(new_beacon->name),sizeof(char));
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
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "update_coworkers_table() start");
    if(coworkers==NULL) {
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "    table empty, allocating new table");
        max_coworkers = current_beacon->coworkers + 2;
        coworkers = (Coworker**)calloc(max_coworkers,sizeof(Coworker*));
        char *new_name = (char*)calloc(strlen(new_coworker->name),sizeof(char));
        strcpy(new_name,new_coworker->name);
        coworkers[0] = new_coworker;
        coworkers[0]->name = new_name;
        num_coworkers = 1;
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "    added first coworker: %s",coworkers[0]->name);
        return true;
    }
    else {
        int size = num_coworkers;
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "    table_size: %u",size);
        int i;
        for(i=0; i<size; ++i) {
            // if that coworker already exists in the table
            if(coworkers[i]->id==new_coworker->id) {
                //APP_LOG(APP_LOG_LEVEL_DEBUG, "    coworker found, rejecting");
                free(new_coworker);
                new_coworker = NULL;
                return false;
            }
        }
        if(i<max_coworkers) {
            // add new if not found in table
            //APP_LOG(APP_LOG_LEVEL_DEBUG, "    coworker not found, adding new");
            char *new_name = (char*)calloc(strlen(new_coworker->name),sizeof(char));
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
            max_coworkers += 4;
            Coworker **new_table = (Coworker**)calloc(max_coworkers,sizeof(Coworker*));
            if(new_table!=NULL) {
                for(i=0; i<size; ++i)
                    new_table[i] = coworkers[i];
                free(coworkers);
                coworkers = new_table;
                char *new_name = (char*)calloc(strlen(new_coworker->name),sizeof(char));
                strcpy(new_name,new_coworker->name);
                coworkers[i] = new_coworker;
                coworkers[i]->name = new_name;
                num_coworkers++;
            }
        }
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "update_coworkers_table() end");
        return true;
    }
    return false;
}

static bool update_achievements_table(Achievement *new_achievement) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "update_achievements_table() start id: %u | name: %s | description: %s",new_achievement->id,new_achievement->name,new_achievement->description);
    if(achievements==NULL) {
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "    table empty, allocating new table");
        achievements = (Achievement**)calloc(max_achievements,sizeof(Achievement*));
        char *new_name = (char*)calloc(strlen(new_achievement->name),sizeof(char));
        strcpy(new_name,new_achievement->name);
        char *new_description = (char*)calloc(strlen(new_achievement->description),sizeof(char));
        strcpy(new_description,new_achievement->description);
        achievements[0] = new_achievement;
        achievements[0]->name = new_name;
        achievements[0]->description = new_description;
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
                free(new_achievement);
                new_achievement = NULL;
                return false;
            }
        }
        // add new if not found in table
        if(i<max_achievements) {
            //APP_LOG(APP_LOG_LEVEL_DEBUG, "    achievement not found, adding new");
            char *new_name = (char*)calloc(strlen(new_achievement->name),sizeof(char));
            strcpy(new_name,new_achievement->name);
            char *new_description = (char*)calloc(strlen(new_achievement->description),sizeof(char));
            strcpy(new_description,new_achievement->description);
            achievements[i] = new_achievement;
            achievements[i]->name = new_name;
            achievements[i]->description = new_description;
            num_achievements++;
        }
        // reallocate table and increase its size twice if new element doesn't fit
        else {
            //APP_LOG(APP_LOG_LEVEL_DEBUG, "    achievement not found, reallocating table and adding new");
            max_achievements *= 2;
            Achievement **new_table = (Achievement**)calloc(max_achievements,sizeof(Achievement*));
            if(new_table!=NULL) {
                for(i=0; i<size; ++i)
                    new_table[i] = achievements[i];
                free(achievements);
                achievements = new_table;
                char *new_name = (char*)calloc(strlen(new_achievement->name),sizeof(char));
                strcpy(new_name,new_achievement->name);
                char *new_description = (char*)calloc(strlen(new_achievement->description),sizeof(char));
                strcpy(new_description,new_achievement->description);
                achievements[i] = new_achievement;
                achievements[i]->name = new_name;
                achievements[i]->description = new_description;
                num_achievements++;
            }
        }
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "update_achievements_table() end");
        return true;
    }
}

static void clear_beacons_table() {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "clear_beacons_table() start");
    if(beacons!=NULL) {
        int size = num_beacons;
        int i;
        for(i=0; i<size; ++i) {
            if(beacons[i]!=NULL) {
                if(beacons[i]->name!=NULL) {
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
    num_beacons_in_range = 0;
    num_beacons_out_of_range = 0;
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "clear_beacons_table() end");
}

static void clear_coworkers_table() {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "clear_coworkers_table() start");
    if(coworkers!=NULL) {
        int size = num_coworkers;
        int i;
        for(i=0; i<size && i<max_coworkers; ++i) {
            if(coworkers[i]!=NULL) {
                if(coworkers[i]->name!=NULL) {
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
    if(achievements!=NULL) {
        int size = num_achievements;
        int i;
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "clear_achievements_table() size: %i",size);
        for(i=0; i<size && i<max_achievements; ++i) {
            if(achievements[i]!=NULL) {
                if(achievements[i]->name!=NULL) {
                    free(achievements[i]->name);
                    achievements[i]->name = NULL;
                }
                if(achievements[i]->description!=NULL) {
                    free(achievements[i]->description);
                    achievements[i]->description = NULL;
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
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "pop_coworker_from_table() 1");
    while(coworkers[i]->id!=id) {
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "pop_coworker_from_table() 2: %i",i);
        i++;
        if(i==size)
            return false;
    }
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "pop_coworker_from_table() 3");
    free(coworkers[i]->name);
    free(coworkers[i]);
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "pop_coworker_from_table() 4");
    for( ; i<size-1; ++i)
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "pop_coworker_from_table() 5: %i",i);
        coworkers[i] = coworkers[i+1];
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "pop_coworker_from_table() 6");
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

enum {
    // request keys
    REQUEST_TYPE = 1,
    REQUEST_QUERY = 2,
    
    // request values
    REQUEST_USER = 1,
    REQUEST_BEACONS = 2,
    REQUEST_COWORKERS = 3,
    REQUEST_ACHIEVEMENTS = 4,
    
    // response keys
    RESPONSE_TYPE = 1,
    USER_NAME = 2,
    USER_LOCATION = 3,
    USER_POINTS = 4,
    USER_RANK = 5,
    USER_BEACONS = 6,
    BEACON_ID = 2,
    BEACON_NAME = 3,
    BEACON_COWORKERS = 4,
    COWORKER_ID = 2,
    COWORKER_NAME = 3,
    ACHIEVEMENT_ID = 2,
    ACHIEVEMENT_NAME = 3,
    ACHIEVEMENT_DESCRIPTION = 4,
    
    // response values
    RESPONSE_USER = 1,
    RESPONSE_BEACON = 2,
    RESPONSE_COWORKER = 3,
    RESPONSE_ACHIEVEMENT = 4,
    RESPONSE_COWORKER_POP = 5
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
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "send_query_request() Request: %u Query: %s start",request,query);
    DictionaryIterator *iter;
    app_message_outbox_begin(&iter);
    
    Tuplet value1 = TupletInteger(REQUEST_TYPE,request);
    Tuplet value2 = TupletInteger(REQUEST_QUERY,query);
    dict_write_tuplet(iter,&value1);
    dict_write_tuplet(iter,&value2);
    dict_write_end(iter);
    
    last_request = request;
    
    app_message_outbox_send();
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "send_query_request() Request: %u Query: %s end",request,query);
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
        //is_downloading = true;
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "Receiving_type: %u",*(receiving_type->value->data));
        if(*(receiving_type->value->data)==RESPONSE_USER) {
            //APP_LOG(APP_LOG_LEVEL_DEBUG, "Starting receiving user");
            Tuple *name = dict_find(iter,USER_NAME);
            Tuple *location = dict_find(iter,USER_LOCATION);
            Tuple *points = dict_find(iter,USER_POINTS);
            Tuple *rank = dict_find(iter,USER_RANK);
            Tuple *beacons = dict_find(iter,USER_BEACONS);
            if(beacons && rank && points && location && name) {
                if(user.name==NULL) {
                    char *new_name = (char*)calloc(strlen(name->value->cstring),sizeof(char));
                    strcpy(new_name,name->value->cstring);
                    user.name = new_name;
                    char *new_location = (char*)calloc(strlen(location->value->cstring),sizeof(char));
                    strcpy(new_location,location->value->cstring);
                    user.location = new_location;
                }
                else {
                    if(strcmp(user.name,name->value->cstring)!=0) {
                        char *new_name = (char*)calloc(strlen(name->value->cstring),sizeof(char));
                        strcpy(new_name,name->value->cstring);
                        free(user.name);
                        user.name = new_name;
                    }
                    if(strcmp(user.location,location->value->cstring)!=0) {
                        char *new_location = (char*)calloc(strlen(location->value->cstring),sizeof(char));
                        strcpy(new_location,location->value->cstring);
                        free(user.location);
                        user.location = new_location;
                    }
                }
                user.points = *(points->value->data);
                user.rank = *(rank->value->data);
                user.beacons = *(beacons->value->data);
                //APP_LOG(APP_LOG_LEVEL_DEBUG, "Recieved user: %s | points: %u | rank: %u | beacons: %u",user.name,user.points,user.rank,user.beacons);
                static char text_buffer[50];
                snprintf(text_buffer,50,"%s\n(%s)",user.name,user.location);
                text_layer_set_text(login_lowertext_layer,text_buffer);
                layer_set_hidden(simple_menu_layer_get_layer(login_menu_layer),false);
            }
            else {
                //APP_LOG(APP_LOG_LEVEL_DEBUG, "Incorrect user dictionary");
            }
        }
        else if(*(receiving_type->value->data)==RESPONSE_BEACON) {
            //APP_LOG(APP_LOG_LEVEL_DEBUG, "Starting receiving beacon");
            Tuple *id = dict_find(iter,BEACON_ID);
            Tuple *name = dict_find(iter,BEACON_NAME);
            //Tuple *proximity = dict_find(iter,BEACON_PROXIMITY);
            Tuple *coworkers = dict_find(iter,BEACON_COWORKERS);
            Beacon *new_beacon = (Beacon*)malloc(sizeof(Beacon));
            if(new_beacon && coworkers /*&& proximity */&& name && id) {
                //APP_LOG(APP_LOG_LEVEL_DEBUG, "strlen(name->value->cstring): %u",strlen(name->value->cstring));
                char new_name[strlen(name->value->cstring)+1];
                strncpy(new_name,name->value->cstring,sizeof(new_name));
                new_beacon->id = *(id->value->data);
                new_beacon->name = new_name;
                //new_beacon->proximity = *(proximity->value->data);
                new_beacon->coworkers = *(coworkers->value->data);
                //APP_LOG(APP_LOG_LEVEL_DEBUG, "Recieving beacon: %s | coworkers: %u",new_beacon->name,new_beacon->coworkers);
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
                layer_mark_dirty(beacons_downloading_sign_layer);
            }
        }
        else if(*(receiving_type->value->data)==RESPONSE_COWORKER) {
            //APP_LOG(APP_LOG_LEVEL_DEBUG, "Starting receiving coworker");
            Tuple *id = dict_find(iter,COWORKER_ID);
            Tuple *name = dict_find(iter,COWORKER_NAME);
            Coworker *new_coworker = (Coworker*)malloc(sizeof(Coworker));
            if(new_coworker && name && id) {
                char new_name[strlen(name->value->cstring)+1];
                strncpy(new_name,name->value->cstring,sizeof(new_name));
                new_coworker->id = *(id->value->data);
                new_coworker->name = new_name;
                //APP_LOG(APP_LOG_LEVEL_DEBUG, "Recieved coworker: %s",new_coworker->name);
                if(update_coworkers_table(new_coworker)) {
                    //APP_LOG(APP_LOG_LEVEL_DEBUG, "Reloading coworkers_menu_layer");
                    menu_layer_reload_data(coworkers_menu_layer);
                }
            }
            else {
                //APP_LOG(APP_LOG_LEVEL_DEBUG, "Incorrect coworker dictionary");
            }
            if(num_coworkers==current_beacon->coworkers) {
                is_downloading = false;
                layer_mark_dirty(coworkers_downloading_sign_layer);
            }
            if(coworkers!=NULL) {
                layer_set_hidden(text_layer_get_layer(coworkers_text_layer),true);
                layer_set_hidden(menu_layer_get_layer(coworkers_menu_layer),false);
            }
        }
        else if(*(receiving_type->value->data)==RESPONSE_ACHIEVEMENT) {
            //APP_LOG(APP_LOG_LEVEL_DEBUG, "Starting receiving achievement");
            Tuple *id = dict_find(iter,ACHIEVEMENT_ID);
            Tuple *name = dict_find(iter,ACHIEVEMENT_NAME);
            Tuple *description = dict_find(iter,ACHIEVEMENT_DESCRIPTION);
            Achievement *new_achievement = (Achievement*)malloc(sizeof(Achievement));
            if(new_achievement && description && name && id) {
                char new_name[strlen(name->value->cstring)+1];
                strncpy(new_name,name->value->cstring,sizeof(new_name));
                char new_description[strlen(description->value->cstring)+1];
                strncpy(new_description,description->value->cstring,sizeof(new_description));
                new_achievement->id = *(id->value->data);
                new_achievement->name = new_name;
                new_achievement->description = new_description;
                //APP_LOG(APP_LOG_LEVEL_DEBUG, "Recieved achievement: id: %i | name: %s | description: %s",new_achievement->id,new_achievement->name,new_achievement->description);
                if(update_achievements_table(new_achievement)) {
                    if(window_stack_get_top_window()==achievements_window) {
                        //APP_LOG(APP_LOG_LEVEL_DEBUG, "Reloading achievements_menu_layer");
                        menu_layer_reload_data(achievements_menu_layer);
                    }
                }
                is_downloading = false;
                layer_mark_dirty(achievements_downloading_sign_layer);
            }
            else {
                //APP_LOG(APP_LOG_LEVEL_DEBUG, "Incorrect achievement dictionary");
            }
        }
        else if(*(receiving_type->value->data)==RESPONSE_COWORKER_POP && window_stack_get_top_window()==coworkers_window) {
            //APP_LOG(APP_LOG_LEVEL_DEBUG, "Starting receiving coworker to pop");
            Tuple *id = dict_find(iter,ACHIEVEMENT_ID);
            if(id) {
                //APP_LOG(APP_LOG_LEVEL_DEBUG, "Popping coworker: id: %i",*(id->value->data));
                if(pop_coworker_from_table(*(id->value->data))) {
                    if(window_stack_get_top_window()==coworkers_window) {
                        //APP_LOG(APP_LOG_LEVEL_DEBUG, "Reloading coworkers_menu_layer");
                        menu_layer_reload_data(coworkers_menu_layer);
                    }
                }
                is_downloading = false;
                layer_mark_dirty(coworkers_downloading_sign_layer);
                if(coworkers==NULL) {
                    layer_set_hidden(menu_layer_get_layer(coworkers_menu_layer),true);
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

///////////////////////////////////// LOGIN WINDOW
static AppTimer *timer;
static SimpleMenuSection login_menu_sections[1];
static SimpleMenuItem login_menu_first_section_items[2];
static GBitmap *beacons_icon;
static GBitmap *user_icon;

static void login_menu_beacons_callback(int index, void *ctx) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "login_menu_beacons_callback()");
    if(user.name!=NULL) {
        if(beacons==NULL) {
            send_simple_request(REQUEST_BEACONS);
            is_downloading = true;
        }
        window_stack_push(beacons_window, true);
    }
}

static void login_menu_user_callback(int index, void *ctx) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "login_menu_user_callback()");
    if(user.name!=NULL) {
        window_stack_push(user_window, true);
    }
}

static void login_request_sending(void *data) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "login_request_sending()");
    if(user.name==NULL) {
        send_simple_request(REQUEST_USER);
        timer = app_timer_register(1000,login_request_sending,NULL);
    }
}

static void login_window_load(Window *window) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "login_window_load() start");
    timer = app_timer_register(2000,login_request_sending,NULL);
    send_simple_request(REQUEST_USER);
    
    beacons_icon = gbitmap_create_with_resource(RESOURCE_ID_ICON_BEACON);
    user_icon = gbitmap_create_with_resource(RESOURCE_ID_ICON_USER);
    
    Layer *window_layer = window_get_root_layer(window);
    int uppertext_height = 26;
    int lowertext_height = 53;
    
    GRect uppertext_bounds = layer_get_bounds(window_layer);
    uppertext_bounds.size.h = uppertext_height;
    login_uppertext_layer = text_layer_create(uppertext_bounds);
    text_layer_set_text(login_uppertext_layer,"Get Results!");
    text_layer_set_font(login_uppertext_layer,fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD));
    text_layer_set_text_alignment(login_uppertext_layer, GTextAlignmentCenter);
    
    GRect lowertext_bounds = layer_get_bounds(window_layer);
    lowertext_bounds.size.h = lowertext_height;
    lowertext_bounds.origin.y += uppertext_height;
    login_lowertext_layer = text_layer_create(lowertext_bounds);
    text_layer_set_text(login_lowertext_layer,"connecting...");
    text_layer_set_font(login_lowertext_layer,fonts_get_system_font(FONT_KEY_GOTHIC_24));
    text_layer_set_text_alignment(login_lowertext_layer, GTextAlignmentCenter);
    
    login_menu_first_section_items[0] = (SimpleMenuItem) {
        .title = "Locations",
        .callback = login_menu_beacons_callback,
        .icon = beacons_icon
    };
    login_menu_first_section_items[1] = (SimpleMenuItem) {
        .title = "User details",
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
    simple_menu_layer_destroy(login_menu_layer);
    text_layer_destroy(login_lowertext_layer);
    text_layer_destroy(login_uppertext_layer);
    gbitmap_destroy(user_icon);
    gbitmap_destroy(beacons_icon);
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "login_window_unload() end");
}
/////////////////////////////////////

///////////////////////////////////// USER WINDOW
static SimpleMenuSection user_menu_sections[1];
static SimpleMenuItem user_menu_first_section_items[1];

//static GBitmap *achievements_icon;

static void user_menu_achievements_callback(int index, void *ctx) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "user_menu_achievements_callback()");
    if(achievements==NULL) {
        send_simple_request(REQUEST_ACHIEVEMENTS);
        is_downloading = true;
    }
    window_stack_push(achievements_window, true);
}

static void user_window_load(Window *window) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "user_window_load() start");
    //achievements_icon = gbitmap_create_with_resource(RESOURCE_ID_ICON_BEACON);
    
    Layer *window_layer = window_get_root_layer(window);
    int text_layer_height = 100;
    
    set_textbar_layer(window_layer,&user_textbar_layer);
    text_layer_set_text(user_textbar_layer,user.name);

    GRect text_bounds = layer_get_bounds(window_layer);
    text_bounds.size.h = text_layer_height;
    text_bounds.origin.y += textbar_height;
    user_text_layer = text_layer_create(text_bounds);
    static char text_buffer[40];
    snprintf(text_buffer,40," Points: %i\n Rank: %i",user.points,user.rank);
    text_layer_set_text(user_text_layer,text_buffer);
    text_layer_set_font(user_text_layer,fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD));
    text_layer_set_text_alignment(user_text_layer, GTextAlignmentLeft);
    
    user_menu_first_section_items[0] = (SimpleMenuItem) {
        .title = "Achievements",
        .callback = user_menu_achievements_callback
        //.icon = achievements_icon
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
    layer_add_child(window_layer, text_layer_get_layer(user_text_layer));
    layer_add_child(window_layer, simple_menu_layer_get_layer(user_menu_layer));
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "user_window_load() end");
}

static void user_window_unload(Window *window) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "user_window_unload() start");
    simple_menu_layer_destroy(user_menu_layer);
    text_layer_destroy(user_text_layer);
    text_layer_destroy(user_textbar_layer);
    //gbitmap_destroy(achievements_icon);
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "user_window_unload() end");
}
/////////////////////////////////////

///////////////////////////////////// BEACONS WINDOW
static void beacons_downloading_sign_layer_update(Layer *layer, GContext *ctx) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "downloading_sign_layer_update() start");
    if(is_downloading && last_request==REQUEST_BEACONS) {
        graphics_context_set_stroke_color(ctx, GColorWhite);
        graphics_context_set_fill_color(ctx, GColorWhite);
        graphics_fill_circle (ctx,GPoint(textbar_height/2,textbar_height/2),textbar_height/4);
    }
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "downloading_sign_layer_update() end");
}

static uint16_t get_num_sections_beacons(MenuLayer *menu_layer, void *data) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "get_num_sections_beacons()");
    //return 2;
    return 1;
}

static int16_t get_header_height_beacons(MenuLayer *menu_layer, uint16_t section_index, void *data) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "get_header_height_beacons()");
    return MENU_CELL_BASIC_HEADER_HEIGHT;
}

static int16_t get_cell_height_beacons(MenuLayer *menu_layer, MenuIndex *cell_index, void *data) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "get_cell_height_beacons()");
    return 26;
}

static void draw_beacon_header(GContext* ctx, const Layer *cell_layer, uint16_t section_index, void *data) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "draw_beacon_header()");
    if(beacons!=NULL) {
        switch (section_index) {
            case 0:
                menu_cell_basic_header_draw(ctx, cell_layer, "Locations");
                break;
        }
    }
    else
        menu_cell_basic_header_draw(ctx, cell_layer, "Downloading locations...");
}

static void draw_beacon_row(GContext *ctx, const Layer *cell_layer, MenuIndex *cell_index, void *callback_context) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "draw_beacon_row()");
    static char text_buffer[40];
    switch (cell_index->section) {
        case 0:
            if(beacons!=NULL) {
                snprintf(text_buffer,40,"(%i) %s",beacons[cell_index->row]->coworkers,beacons[cell_index->row]->name);
                menu_cell_basic_draw(ctx, cell_layer, text_buffer, NULL, NULL);
            }
            break;
    }
}

static void beacon_select_click(struct MenuLayer *menu_layer, MenuIndex *cell_index, void *callback_context) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "beacon_select_click()");
    if(cell_index->section==0 && beacons!=NULL) {
        current_beacon = beacons[cell_index->row];
    }
    
    if(current_beacon!=NULL /*&& current_beacon->coworkers>0*/) {
        if(previous_beacon!=current_beacon) {
            clear_coworkers_table();
            send_query_request(REQUEST_COWORKERS,current_beacon->id);
            is_downloading = true;
        }
        previous_beacon = current_beacon;
        window_stack_push(coworkers_window, true);
    }
}

MenuLayerCallbacks beacons_menu_callbacks = {
    .get_num_sections = get_num_sections_beacons,
    .get_num_rows = get_num_beacons,
    .get_header_height = get_header_height_beacons,
    .get_cell_height = get_cell_height_beacons,
    .draw_header = draw_beacon_header,
    .draw_row = draw_beacon_row,
    .select_click = beacon_select_click
};

static void beacons_window_load(Window *window) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "beacons_window_load() start");
    Layer *window_layer = window_get_root_layer(window);
    
    set_textbar_layer(window_layer,&beacons_textbar_layer);
    text_layer_set_text(beacons_textbar_layer,user.name);
    
    GRect downloading_sign_layer_bounds = layer_get_bounds(window_layer);
    downloading_sign_layer_bounds.origin.x = downloading_sign_layer_bounds.size.w-textbar_height;
    downloading_sign_layer_bounds.size.h = textbar_height;
    downloading_sign_layer_bounds.size.w = textbar_height;
    beacons_downloading_sign_layer = layer_create(downloading_sign_layer_bounds);
    layer_set_update_proc(beacons_downloading_sign_layer, beacons_downloading_sign_layer_update);
    
    GRect menu_bounds = layer_get_bounds(window_layer);
    menu_bounds.size.h -= textbar_height;
    menu_bounds.origin.y += textbar_height;
    beacons_menu_layer = menu_layer_create(menu_bounds);
    menu_layer_set_callbacks(beacons_menu_layer, NULL, beacons_menu_callbacks);
    menu_layer_set_click_config_onto_window(beacons_menu_layer, window);
    
    layer_add_child(window_layer, text_layer_get_layer(beacons_textbar_layer));
    layer_add_child(window_layer, beacons_downloading_sign_layer);
    layer_add_child(window_layer, menu_layer_get_layer(beacons_menu_layer));
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "beacons_window_load() end");
}

static void beacons_window_unload(Window *window) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "beacons_window_unload() start");
    menu_layer_destroy(beacons_menu_layer);
    layer_destroy(beacons_downloading_sign_layer);
    text_layer_destroy(beacons_textbar_layer);
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "beacons_window_unload() start");
}

static void beacons_window_appear(Window *window) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "beacons_window_appear() start");
    //send_simple_request(REQUEST_BEACONS);
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "beacons_window_appear() end");
}
/////////////////////////////////////

///////////////////////////////////// COWORKERS WINDOW
static void coworkers_downloading_sign_layer_update(Layer *layer, GContext *ctx) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "downloading_sign_layer_update() start");
    if(is_downloading && last_request==REQUEST_COWORKERS && current_beacon->coworkers>0) {
        graphics_context_set_stroke_color(ctx, GColorWhite);
        graphics_context_set_fill_color(ctx, GColorWhite);
        graphics_fill_circle (ctx,GPoint(textbar_height/2,textbar_height/2),textbar_height/4);
    }
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "downloading_sign_layer_update() end");
}

static uint16_t get_num_sections_coworkers(MenuLayer *menu_layer, void *data) {
    return 1;
}

static int16_t get_header_height_coworkers(MenuLayer *menu_layer, uint16_t section_index, void *data) {
    return MENU_CELL_BASIC_HEADER_HEIGHT;
}

static int16_t get_cell_height_coworkers(MenuLayer *menu_layer, MenuIndex *cell_index, void *data) {
    return 26;
}

static void draw_coworker_header(GContext* ctx, const Layer *cell_layer, uint16_t section_index, void *data) {
    if(coworkers!=NULL) {
        switch (section_index) {
            case 0:
                menu_cell_basic_header_draw(ctx, cell_layer, "Coworkers in room");
                break;
        }
    }
    else
        menu_cell_basic_header_draw(ctx, cell_layer, "This room is empty");
}

static void draw_coworker_row(GContext *ctx, const Layer *cell_layer, MenuIndex *cell_index, void *callback_context) {
    if(coworkers!=NULL) {
        switch (cell_index->section) {
            case 0:
                if(coworkers!=NULL)
                    menu_cell_basic_draw(ctx, cell_layer, coworkers[cell_index->row]->name, NULL, NULL);
                break;
        }
    }
    //else
        //menu_cell_basic_draw(ctx, cell_layer, "NOBODY...", NULL, NULL);
}

static void coworker_select_click(struct MenuLayer *menu_layer, MenuIndex *cell_index, void *callback_context) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "coworker_select_click() start");
    /*
     if(cell_index->section==0) {
     current_coworker = coworkers[cell_index->row];
     }
     
     if(current_coworker!=NULL)
     //APP_LOG(APP_LOG_LEVEL_DEBUG, "coworker selected: %s",current_coworker->name);
     else
     //APP_LOG(APP_LOG_LEVEL_DEBUG, "current_coworker==NULL!");
     
     window_stack_push(coworker_details_window, true); // or maybe user_window???
     */
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "coworker_select_click() end");
}

MenuLayerCallbacks coworkers_menu_callbacks = {
    .get_num_sections = get_num_sections_coworkers,
    .get_num_rows = get_num_coworkers,
    .get_header_height = get_header_height_coworkers,
    .get_cell_height = get_cell_height_coworkers,
    .draw_header = draw_coworker_header,
    .draw_row = draw_coworker_row,
    .select_click = coworker_select_click
};

static void coworkers_window_load(Window *window) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "coworkers_window_load() start");
    Layer *window_layer = window_get_root_layer(window);
    
    set_textbar_layer(window_layer,&coworkers_textbar_layer);
    text_layer_set_text(coworkers_textbar_layer,current_beacon->name);
    
    GRect downloading_sign_layer_bounds = layer_get_bounds(window_layer);
    downloading_sign_layer_bounds.origin.x = downloading_sign_layer_bounds.size.w-textbar_height;
    downloading_sign_layer_bounds.size.h = textbar_height;
    downloading_sign_layer_bounds.size.w = textbar_height;
    coworkers_downloading_sign_layer = layer_create(downloading_sign_layer_bounds);
    layer_set_update_proc(coworkers_downloading_sign_layer, coworkers_downloading_sign_layer_update);
    
    GRect menu_bounds = layer_get_bounds(window_layer);
    menu_bounds.size.h -= textbar_height;
    menu_bounds.origin.y += textbar_height;
    coworkers_menu_layer = menu_layer_create(menu_bounds);
    menu_layer_set_callbacks(coworkers_menu_layer, NULL, coworkers_menu_callbacks);
    menu_layer_set_click_config_onto_window(coworkers_menu_layer, window);
    
    coworkers_text_layer = text_layer_create(menu_bounds);
    text_layer_set_text(coworkers_text_layer,"This room is empty");
    text_layer_set_font(coworkers_text_layer,fonts_get_system_font(FONT_KEY_GOTHIC_24));
    text_layer_set_text_alignment(coworkers_text_layer, GTextAlignmentLeft);
    
    if(current_beacon->coworkers!=0) {
        layer_set_hidden(text_layer_get_layer(coworkers_text_layer),true);
        layer_set_hidden(menu_layer_get_layer(coworkers_menu_layer),false);
    } 
    else {
        layer_set_hidden(menu_layer_get_layer(coworkers_menu_layer),true);
        layer_set_hidden(text_layer_get_layer(coworkers_text_layer),false);
    }
        
    
    layer_add_child(window_layer, text_layer_get_layer(coworkers_textbar_layer));
    layer_add_child(window_layer, coworkers_downloading_sign_layer);
    layer_add_child(window_layer, menu_layer_get_layer(coworkers_menu_layer));
    layer_add_child(window_layer, text_layer_get_layer(coworkers_text_layer));
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "coworkers_window_load() end");
}

static void coworkers_window_unload(Window *window) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "coworkers_window_unload() start");
    text_layer_destroy(coworkers_text_layer);
    menu_layer_destroy(coworkers_menu_layer);
    layer_destroy(coworkers_downloading_sign_layer);
    text_layer_destroy(coworkers_textbar_layer);
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "coworkers_window_unload() end");
}
/////////////////////////////////////

///////////////////////////////////// ACHIEVEMENTS WINDOW
static void achievements_downloading_sign_layer_update(Layer *layer, GContext *ctx) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "achievements_downloading_sign_layer_update() start");
    if(is_downloading && last_request==REQUEST_ACHIEVEMENTS) {
        graphics_context_set_stroke_color(ctx, GColorWhite);
        graphics_context_set_fill_color(ctx, GColorWhite);
        graphics_fill_circle (ctx,GPoint(textbar_height/2,textbar_height/2),textbar_height/4);
    }
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "achievements_downloading_sign_layer_update() end");
}

static uint16_t get_num_sections_achievements(MenuLayer *menu_layer, void *data) {
    if(achievements!=NULL)
        return 1;
    else
        return 0;
}

static int16_t get_header_height_achievements(MenuLayer *menu_layer, uint16_t section_index, void *data) {
    return MENU_CELL_BASIC_HEADER_HEIGHT;
}

static int16_t get_cell_height_achievements(MenuLayer *menu_layer, MenuIndex *cell_index, void *data) {
    return 26;
}

static void draw_achievement_header(GContext* ctx, const Layer *cell_layer, uint16_t section_index, void *data) {
    if(achievements!=NULL) {
        switch (section_index) {
            case 0:
                menu_cell_basic_header_draw(ctx, cell_layer, "Achievements");
                break;
        }
    }
    else
        menu_cell_basic_header_draw(ctx, cell_layer, "You haven't gained any achievement");
}

static void draw_achievement_row(GContext *ctx, const Layer *cell_layer, MenuIndex *cell_index, void *callback_context) {
    if(achievements!=NULL) {
        switch (cell_index->section) {
            case 0:
                menu_cell_basic_draw(ctx, cell_layer, achievements[cell_index->row]->name, NULL, NULL);
                break;
        }
    }
    else
        menu_cell_basic_draw(ctx, cell_layer, "NOTHING...", NULL, NULL);
}

static void achievement_select_click(struct MenuLayer *menu_layer, MenuIndex *cell_index, void *callback_context) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "achievement_select_click()");
    if(cell_index->section==0 && achievements!=NULL)
        current_achievement = achievements[cell_index->row];
    if(current_achievement!=NULL)
        window_stack_push(achievement_details_window, true);
}

MenuLayerCallbacks achievements_menu_callbacks = {
    .get_num_sections = get_num_sections_achievements,
    .get_num_rows = get_num_achievements,
    .get_header_height = get_header_height_achievements,
    .get_cell_height = get_cell_height_achievements,
    .draw_header = draw_achievement_header,
    .draw_row = draw_achievement_row,
    .select_click = achievement_select_click
};

static void achievements_window_load(Window *window) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "achievements_window_load() start");
    Layer *window_layer = window_get_root_layer(window);
    
    set_textbar_layer(window_layer,&achievements_textbar_layer);
    text_layer_set_text(achievements_textbar_layer,user.name);
    
    GRect downloading_sign_layer_bounds = layer_get_bounds(window_layer);
    downloading_sign_layer_bounds.origin.x = downloading_sign_layer_bounds.size.w-textbar_height;
    downloading_sign_layer_bounds.size.h = textbar_height;
    downloading_sign_layer_bounds.size.w = textbar_height;
    achievements_downloading_sign_layer = layer_create(downloading_sign_layer_bounds);
    layer_set_update_proc(achievements_downloading_sign_layer, achievements_downloading_sign_layer_update);
    
    GRect menu_bounds = layer_get_bounds(window_layer);
    menu_bounds.size.h -= textbar_height;
    menu_bounds.origin.y += textbar_height;
    achievements_menu_layer = menu_layer_create(menu_bounds);
    menu_layer_set_callbacks(achievements_menu_layer, NULL, achievements_menu_callbacks);
    menu_layer_set_click_config_onto_window(achievements_menu_layer, window);
    
    layer_add_child(window_layer, text_layer_get_layer(achievements_textbar_layer));
    layer_add_child(window_layer, achievements_downloading_sign_layer);
    layer_add_child(window_layer, menu_layer_get_layer(achievements_menu_layer));
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "achievements_window_load() end");
}

static void achievements_window_unload(Window *window) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "achievements_window_unload() start");
    menu_layer_destroy(achievements_menu_layer);
    layer_destroy(achievements_downloading_sign_layer);
    text_layer_destroy(achievements_textbar_layer);
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "achievements_window_unload() end");
}
/////////////////////////////////////

///////////////////////////////////// ACHIEVEMENT DETAILS WINDOW
static void achievement_details_window_load(Window *window) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "achievement_details_window_load() start");
    Layer *window_layer = window_get_root_layer(window);

    set_textbar_layer(window_layer,&achievement_details_textbar_layer);
    text_layer_set_text(achievement_details_textbar_layer,current_achievement->name);
    
    GRect max_text_bounds = GRect(0,0,layer_get_bounds(window_layer).size.w,500);
    achievement_details_text_layer = text_layer_create(max_text_bounds);
    text_layer_set_text(achievement_details_text_layer,current_achievement->description);
    text_layer_set_font(achievement_details_text_layer,fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD));
    text_layer_set_text_alignment(achievement_details_text_layer, GTextAlignmentCenter);
    
    GRect scroll_layer_bounds = layer_get_bounds(window_layer);
    scroll_layer_bounds.size.h -= textbar_height;
    scroll_layer_bounds.origin.y = textbar_height;
    achievement_details_scroll_layer = scroll_layer_create(scroll_layer_bounds);
    scroll_layer_set_click_config_onto_window(achievement_details_scroll_layer,achievement_details_window);
    GSize max_size = text_layer_get_content_size(achievement_details_text_layer);
    max_size.w = scroll_layer_bounds.size.w;
    max_size.h += 10;
    text_layer_set_size(achievement_details_text_layer,max_size);
    scroll_layer_set_content_size(achievement_details_scroll_layer,GSize(max_text_bounds.size.w,max_size.h));
    scroll_layer_add_child(achievement_details_scroll_layer,text_layer_get_layer(achievement_details_text_layer));
    
    layer_add_child(window_layer, text_layer_get_layer(achievement_details_textbar_layer));
    layer_add_child(window_layer,scroll_layer_get_layer(achievement_details_scroll_layer));
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "achievement_details_window_load() end");
}

static void achievement_details_window_unload(Window *window) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "achievement_details_window_unload() start");
    text_layer_destroy(achievement_details_text_layer);
    scroll_layer_destroy(achievement_details_scroll_layer);
    text_layer_destroy(achievement_details_textbar_layer);
    current_achievement = NULL;
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
    .unload = beacons_window_unload,
    .appear = beacons_window_appear
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
    num_beacons_in_range = 0;
    num_beacons_out_of_range = 0;
    num_coworkers = 0;
    num_achievements = 0;
    max_achievements = 2;
    last_request = 0;
    
    is_downloading = false;
    
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
    
    window_stack_push(login_window, true);
}

static void deinit() {
    window_destroy(achievement_details_window);
    window_destroy(achievements_window);
    window_destroy(coworkers_window);
    window_destroy(beacons_window);
    window_destroy(user_window);
    window_destroy(login_window);
    
    if(user.name!=NULL) {
        free(user.name);
        user.name = NULL;
    }
    if(user.location!=NULL) {
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
