///    @author: Pawel Palka
///    @email: ppalka@sosoftware.pl    // company
///    @email: fractalord@gmail.com    // private

///    W OSTATECZNEJ WERSJI USUNAC LOGI (te niezakomentowane), ZAJMUJA OK 1KB (w pliku binarnym)!!!

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
static TextLayer *achievements_text_layer;
static BitmapLayer *achievements_downloading_sign_layer;

static Window *achievement_details_window;
static TextLayer *achievement_details_textbar_layer;
static TextLayer *achievement_details_title_text_layer;
static TextLayer *achievement_details_content_text_layer;
static ScrollLayer *achievement_details_scroll_layer;
static BitmapLayer *achievement_details_badge_layer;
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
    int beacons_pages;
    int achievements;
    int achievements_pages;
    bool logged_on;
} User;

typedef struct {
    int id;
    char *name;
    int coworkers;
    int coworkers_pages;
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

static int previous_achievement_id;
static int previous_beacon_id;

static User user;
static Beacon **beacons;
static Coworker **coworkers;
static Achievement **achievements;

static uint16_t num_beacons;
static uint16_t max_beacons;
static uint16_t num_coworkers;
static uint16_t max_coworkers;
static uint16_t num_achievements;
static uint16_t max_achievements;

static int current_beacons_page;
static int current_coworkers_page;
static int current_achievements_page;

static uint8_t last_request;
static uint8_t max_list_size = 11;

static bool is_downloading;
static bool animated = false;

static char current_achievement_description[255];
static uint8_t current_achievement_badge_data[8*64];
static GBitmap current_achievement_badge_bitmap;
static bool badge_loaded;

static int textbar_height = 24;

static uint16_t get_num_beacons(struct MenuLayer* menu_layer, uint16_t section_index, void *callback_context) {
    if(user.beacons_pages>0) {
        if(current_beacons_page<user.beacons_pages) {
            if(current_beacons_page==1)
                return num_beacons+1;
            else
                return num_beacons+2;
        }
        else {
            if(current_beacons_page==1)
                return num_beacons;
            else
                return num_beacons+1;
        }
    }
    else
        return 0;
}

static uint16_t get_num_coworkers(struct MenuLayer* menu_layer, uint16_t section_index, void *callback_context) {
    if(current_beacon->coworkers_pages>0) {
        if(current_coworkers_page<current_beacon->coworkers_pages) {
            if(current_coworkers_page==1)
                return num_coworkers+1;
            else
                return num_coworkers+2;
        }
        else {
            if(current_coworkers_page==1)
                return num_coworkers;
            else
                return num_coworkers+1;
        }
    }
    else
        return 0;
}

static uint16_t get_num_achievements(struct MenuLayer* menu_layer, uint16_t section_index, void *callback_context) {
    if(user.achievements_pages>0) {
        if(current_achievements_page<user.achievements_pages) {
            if(current_achievements_page==1)
                return num_achievements+1;
            else
                return num_achievements+2;
        }
        else {
            if(current_achievements_page==1)
                return num_achievements;
            else
                return num_achievements+1;
        }
    }
    else
        return 0;
}

static int beacons_compare(const void *b1, const void *b2) {
    char *n1 = (*(Beacon**)b1)->name;
    char *n2 = (*(Beacon**)b2)->name;
    return strcmp(n1,n2);
}

static bool update_beacons_table(Beacon *new_beacon) {
    if(beacons==NULL) {
        max_beacons = max_list_size;
        beacons = (Beacon**)malloc(max_beacons*sizeof(Beacon*));
        char *new_name = (char*)malloc((strlen(new_beacon->name)+1)*sizeof(char));
        strcpy(new_name,new_beacon->name);
        beacons[0] = new_beacon;
        beacons[0]->name = new_name;
        num_beacons = 1;
        return true;
    }
    else {
        int size = num_beacons;
        int i;
        for(i=0; i<size; ++i) {
            // if that beacon already exists in the table
            if(beacons[i]->id==new_beacon->id) {
                beacons[i]->coworkers = new_beacon->coworkers;
                beacons[i]->coworkers_pages = new_beacon->coworkers_pages;
                if(new_beacon) {
                    free(new_beacon);
                    new_beacon = NULL;
                }
                return true;
            }
        }
        if(i<max_beacons) {
            // add new if not found in table
            char *new_name = (char*)malloc((strlen(new_beacon->name)+1)*sizeof(char));
            strcpy(new_name,new_beacon->name);
            beacons[i] = new_beacon;
            beacons[i]->name = new_name;
            num_beacons++;
            qsort(beacons,num_beacons,sizeof(Beacon*),beacons_compare);
            return true;
        }
    }
    return false;
}

static bool update_coworkers_table(Coworker *new_coworker) {
    if(current_beacon->id==new_coworker->beacon_id) {
        if(coworkers==NULL) {
            max_coworkers = max_list_size;
            coworkers = (Coworker**)malloc(max_coworkers*sizeof(Coworker*));
            char *new_name = (char*)malloc((strlen(new_coworker->name)+1)*sizeof(char));
            strcpy(new_name,new_coworker->name);
            coworkers[0] = new_coworker;
            coworkers[0]->name = new_name;
            num_coworkers = 1;
            return true;
        }
        else {
            int size = num_coworkers;
            int i;
            for(i=0; i<size; ++i) {
                // if that coworker already exists in the table
                if(coworkers[i]->id==new_coworker->id) {
                    if(new_coworker) {
                        free(new_coworker);
                        new_coworker = NULL;
                    }
                    return false;
                }
            }
            if(i<max_coworkers) {
                // add new if not found in table
                char *new_name = (char*)malloc((strlen(new_coworker->name)+1)*sizeof(char));
                strcpy(new_name,new_coworker->name);
                coworkers[i] = new_coworker;
                coworkers[i]->name = new_name;
                num_coworkers++;
                return true;
            }
        }
    }
    else {
        if(new_coworker) {
            free(new_coworker);
            new_coworker = NULL;
        }
    }
    return false;
}

static bool update_achievements_table(Achievement *new_achievement) {
    if(achievements==NULL) {
        max_achievements = max_list_size;
        achievements = (Achievement**)malloc(max_achievements*sizeof(Achievement*));
        char *new_name = (char*)malloc((strlen(new_achievement->name)+1)*sizeof(char));
        strcpy(new_name,new_achievement->name);
        achievements[0] = new_achievement;
        achievements[0]->name = new_name;
        num_achievements = 1;
        return true;
    }
    else {
        int size = num_achievements;
        int i;
        for(i=0; i<size && i<max_achievements; ++i) {
            // if that achievement already exists in the table
            if(achievements[i]->id==new_achievement->id) {
                if(new_achievement) {
                    free(new_achievement);
                    new_achievement = NULL;
                }
                return false;
            }
        }
        // add new if not found in table
        if(i<max_achievements) {
            char *new_name = (char*)malloc((strlen(new_achievement->name)+1)*sizeof(char));
            strcpy(new_name,new_achievement->name);
            achievements[i] = new_achievement;
            achievements[i]->name = new_name;
            num_achievements++;
            return true;
        }
    }
    return false;
}

static void clear_beacons_table() {
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
}

static void clear_coworkers_table() {
    if(coworkers) {
        int size = num_coworkers;
        int i;
        for(i=0; i<size && i<max_coworkers; ++i) {
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
}

static void clear_achievements_table() {
    if(achievements) {
        int size = num_achievements;
        int i;
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
}

static bool pop_coworker_from_table(int id) {
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
    return true;
}

static bool pop_achievement_from_table(int id) {
    int i=0, size = num_achievements;
    while(achievements[i]->id!=id) {
        i++;
        if(i==size)
            return false;
    }
    if(achievements[i]) {
        if(achievements[i]->name)
            free(achievements[i]->name);
        free(achievements[i]);
    }
    for( ; i<size-1; ++i)
        achievements[i] = achievements[i+1];
    achievements[i] = NULL;
    num_achievements--;
    if(achievements[0]==NULL)
        clear_achievements_table();
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
    REQUEST_QUERY1 = 2,
    REQUEST_QUERY2 = 3,
    
    // request values
    REQUEST_USER = 1,
    REQUEST_BEACONS_PAGE = 2,
    REQUEST_COWORKERS_PAGE = 3,
    REQUEST_ACHIEVEMENTS_PAGE = 4,
    REQUEST_ACHIEVEMENT_CONTENT = 5,
    
    // response keys
    RESPONSE_TYPE = 1,
    USER_NAME = 2,
    USER_LOCATION = 3,
    USER_POINTS = 4,
    USER_RANK = 5,
    USER_BEACONS_PAGES = 6,
    USER_ACHIEVEMENTS = 7,
    USER_ACHIEVEMENTS_PAGES = 8,
    BEACON_ID = 2,
    BEACON_NAME = 3,
    BEACON_COWORKERS = 4,
    BEACON_COWORKERS_PAGES = 5,
    BEACON_PAGE = 6,
    BEACON_MORE = 7,
    COWORKER_ID = 2,
    COWORKER_NAME = 3,
    COWORKER_BEACON_ID = 4,
    COWORKER_PAGE = 5,
    COWORKER_MORE = 6,
    ACHIEVEMENT_ID = 2,
    ACHIEVEMENT_NAME = 3,
    ACHIEVEMENT_DESCRIPTION = 3,
    ACHIEVEMENT_NUMBER = 4,
    ACHIEVEMENT_PAGE = 5,
    ACHIEVEMENT_MORE = 6,
    ACHIEVEMENT_BADGE_OFFSET = 3,
    ACHIEVEMENT_BADGE_DATA = 4,
    
    // response values
    RESPONSE_USER = 1,
    RESPONSE_BEACON = 2,
    RESPONSE_COWORKER = 3,
    RESPONSE_ACHIEVEMENT_HEADER = 4,
    RESPONSE_ACHIEVEMENT_CONTENT = 5,
    RESPONSE_ACHIEVEMENT_BADGE = 6,
    RESPONSE_COWORKER_POP = 7,
    RESPONSE_ACHIEVEMENT_POP = 8
};

///////////////////////////////////// COMMUNICATION
static void send_simple_request(int8_t request) {
    DictionaryIterator *iter;
    app_message_outbox_begin(&iter);
    
    Tuplet value = TupletInteger(REQUEST_TYPE,request);
    dict_write_tuplet(iter,&value);
    dict_write_end(iter);
    
    last_request = request;
    app_message_outbox_send();
}

static void send_query_request(int8_t request, int query1, int query2) {
    DictionaryIterator *iter;
    app_message_outbox_begin(&iter);
    
    Tuplet value1 = TupletInteger(REQUEST_TYPE,request);
    Tuplet value2 = TupletInteger(REQUEST_QUERY1,query1);
    Tuplet value3 = TupletInteger(REQUEST_QUERY2,query2);
    dict_write_tuplet(iter,&value1);
    dict_write_tuplet(iter,&value2);
    dict_write_tuplet(iter,&value3);
    dict_write_end(iter);
    
    last_request = request;
    
    app_message_outbox_send();
    if(window_stack_get_top_window()==coworkers_window && last_request==REQUEST_COWORKERS_PAGE)
        layer_set_hidden(menu_layer_get_layer(coworkers_menu_layer),true);
    else if(window_stack_get_top_window()==achievements_window && last_request==REQUEST_ACHIEVEMENTS_PAGE)
        layer_set_hidden(menu_layer_get_layer(achievements_menu_layer),true);
    else if(window_stack_get_top_window()==beacons_window && last_request==REQUEST_BEACONS_PAGE)
        layer_set_hidden(menu_layer_get_layer(beacons_menu_layer),true);
}

void out_sent_handler(DictionaryIterator *sent, void *context) {
    if(window_stack_get_top_window()==achievements_window && last_request==REQUEST_ACHIEVEMENTS_PAGE)
        layer_set_hidden(menu_layer_get_layer(achievements_menu_layer),false);
    else if(window_stack_get_top_window()==coworkers_window && last_request==REQUEST_COWORKERS_PAGE)
        layer_set_hidden(menu_layer_get_layer(coworkers_menu_layer),false);
    else if(window_stack_get_top_window()==beacons_window && last_request==REQUEST_BEACONS_PAGE)
        layer_set_hidden(menu_layer_get_layer(beacons_menu_layer),false);
}

void out_failed_handler(DictionaryIterator *failed, AppMessageResult reason, void *context) {
}

void in_received_handler(DictionaryIterator *iter, void *context) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "in_received_handler() start dictionary size: %u",(uint16_t)dict_size(iter));
    Tuple *receiving_type = dict_find(iter,RESPONSE_TYPE);
    if(receiving_type) {
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "Receiving_type: %u",receiving_type->value->uint16);
        if(receiving_type->value->uint16==RESPONSE_USER) {
            Tuple *name = dict_find(iter,USER_NAME);
            Tuple *location = dict_find(iter,USER_LOCATION);
            if(location && name) {
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
                user.points = dict_find(iter,USER_POINTS)->value->uint16;
                user.rank = dict_find(iter,USER_RANK)->value->uint16;
                user.beacons_pages = dict_find(iter,USER_BEACONS_PAGES)->value->uint16;
                user.achievements = dict_find(iter,USER_ACHIEVEMENTS)->value->uint16;
                user.achievements_pages = dict_find(iter,USER_ACHIEVEMENTS_PAGES)->value->uint16;
                if(!user.logged_on)
                    fire_login_animation();
                else {
                    static char text_buffer[40];
                    if(strlen(user.location)>0)
                        snprintf(text_buffer,40,"in %s",user.location);
                    else
                        snprintf(text_buffer,40," ");
                    text_layer_set_text(login_lowertext_layer,text_buffer);
                    text_layer_set_font(login_lowertext_layer,fonts_get_system_font(strlen(text_buffer)>13?FONT_KEY_GOTHIC_24:FONT_KEY_GOTHIC_28));
                }
                user.logged_on = true;
                //APP_LOG(APP_LOG_LEVEL_DEBUG, "Recieved user: %s | p: %u | r: %u | bp: %u | a: %u | ap: %u | l: %s",user.name,user.points,user.rank,user.beacons_pages,user.achievements,user.achievements_pages,user.location);
                if(user_right_text_layer) {
                    static char text_buffer[25];
                    snprintf(text_buffer,25,"%i \n%i \n%i ",user.points,user.rank,user.achievements);
                    text_layer_set_text(user_right_text_layer,text_buffer);
                }
                if(achievements_menu_layer) {
                    menu_layer_reload_data(achievements_menu_layer);
                }
            }
        }
        else if(receiving_type->value->uint16==RESPONSE_BEACON && user.logged_on) {
            Tuple *name = dict_find(iter,BEACON_NAME);
            Beacon *new_beacon = (Beacon*)malloc(sizeof(Beacon));
            if(new_beacon && name) {
                char new_name[strlen(name->value->cstring)+1];
                strncpy(new_name,name->value->cstring,sizeof(new_name));
                new_beacon->id = dict_find(iter,BEACON_ID)->value->uint16;
                new_beacon->name = new_name;
                new_beacon->coworkers = dict_find(iter,BEACON_COWORKERS)->value->uint16;
                new_beacon->coworkers_pages = dict_find(iter,BEACON_COWORKERS_PAGES)->value->uint16;
                //APP_LOG(APP_LOG_LEVEL_DEBUG, "Recieving beacon: %s | id: %i | p: %i | c: %i | cp: %i | more: %i",new_beacon->name,new_beacon->id,(int)dict_find(iter,BEACON_PAGE)->value->uint16,new_beacon->coworkers,new_beacon->coworkers_pages,(int)dict_find(iter,BEACON_MORE)->value->uint16);
                if((int)dict_find(iter,BEACON_PAGE)->value->uint16+1==current_beacons_page) {
                    if(update_beacons_table(new_beacon)) {
                        if(beacons_menu_layer) {
                            menu_layer_reload_data(beacons_menu_layer);
                        }
                        if(coworkers_menu_layer) {
                            menu_layer_reload_data(coworkers_menu_layer);
                        }
                        if(!dict_find(iter,BEACON_MORE)->value->uint16) {
                            is_downloading = false;
                            if(beacons_downloading_sign_layer)
                                layer_set_hidden(bitmap_layer_get_layer(beacons_downloading_sign_layer),true);
                        }
                    }
                }
                else {
                    free(new_beacon);
                    new_beacon = NULL;
                }
            }
        }
        else if(receiving_type->value->uint16==RESPONSE_COWORKER && user.logged_on && current_beacon) {
            Tuple *name = dict_find(iter,COWORKER_NAME);
            Coworker *new_coworker = (Coworker*)malloc(sizeof(Coworker));
            if(new_coworker && name) {
                char new_name[strlen(name->value->cstring)+1];
                strncpy(new_name,name->value->cstring,sizeof(new_name));
                new_coworker->id = dict_find(iter,COWORKER_ID)->value->uint16;
                new_coworker->name = new_name;
                new_coworker->beacon_id = dict_find(iter,COWORKER_BEACON_ID)->value->uint16;
                //APP_LOG(APP_LOG_LEVEL_DEBUG, "Recieved coworker: %s | beacon_id: %i | page: %i | more: %i",new_coworker->name,new_coworker->beacon_id,(int)dict_find(iter,COWORKER_PAGE)->value->uint16,(int)dict_find(iter,COWORKER_MORE)->value->uint16);
                if((int)dict_find(iter,COWORKER_PAGE)->value->uint16+1==current_coworkers_page) {
                    if(update_coworkers_table(new_coworker)) {
                        if(coworkers_menu_layer)
                            menu_layer_reload_data(coworkers_menu_layer);
                        if(coworkers && window_stack_get_top_window()==coworkers_window) {
                            if(coworkers_text_layer)
                                layer_set_hidden(text_layer_get_layer(coworkers_text_layer),true);
                            if(coworkers_menu_layer)
                                layer_set_hidden(menu_layer_get_layer(coworkers_menu_layer),false);
                        }
                        if(!dict_find(iter,COWORKER_MORE)->value->uint16) {
                            is_downloading = false;
                            if(coworkers_downloading_sign_layer)
                                layer_set_hidden(bitmap_layer_get_layer(coworkers_downloading_sign_layer),true);
                        }
                    }
                }
                else {
                    free(new_coworker);
                    new_coworker = NULL;
                }
            }
        }
        else if(receiving_type->value->uint16==RESPONSE_ACHIEVEMENT_HEADER && user.logged_on) {
            Tuple *name = dict_find(iter,ACHIEVEMENT_NAME);
            Achievement *new_achievement = (Achievement*)malloc(sizeof(Achievement));
            if(new_achievement && name) {
                char new_name[strlen(name->value->cstring)+1];
                strcpy(new_name,name->value->cstring);
                new_achievement->id = dict_find(iter,ACHIEVEMENT_ID)->value->uint16;
                new_achievement->name = new_name;
                new_achievement->parts = dict_find(iter,ACHIEVEMENT_NUMBER)->value->uint16;
                //APP_LOG(APP_LOG_LEVEL_DEBUG, "Recieved ach. header: id: %i | name: %s | parts: %i | page: %i | more: %i",new_achievement->id,new_achievement->name,new_achievement->parts,(int)dict_find(iter,ACHIEVEMENT_PAGE)->value->uint16,(int)dict_find(iter,ACHIEVEMENT_MORE)->value->uint16);
                if((int)dict_find(iter,ACHIEVEMENT_PAGE)->value->uint16+1==current_achievements_page) {
                    if(update_achievements_table(new_achievement)) {
                        if(achievements_menu_layer) {
                            menu_layer_reload_data(achievements_menu_layer);
                        }
                        if(achievements && window_stack_get_top_window()==achievements_window) {
                            if(achievements_text_layer)
                                layer_set_hidden(text_layer_get_layer(achievements_text_layer),true);
                            if(achievements_menu_layer)
                                layer_set_hidden(menu_layer_get_layer(achievements_menu_layer),false);
                        }
                    }
                    if(!dict_find(iter,ACHIEVEMENT_MORE)->value->uint16) {
                        is_downloading = false;
                        if(achievements_downloading_sign_layer) {
                            layer_set_hidden(bitmap_layer_get_layer(achievements_downloading_sign_layer),true);
                        }
                    }
                }
                else {
                    free(new_achievement);
                    new_achievement = NULL;
                }
            }
        }
        else if(receiving_type->value->uint16==RESPONSE_ACHIEVEMENT_CONTENT && user.logged_on && current_achievement) {
            Tuple *id = dict_find(iter,ACHIEVEMENT_ID);
            Tuple *text = dict_find(iter,ACHIEVEMENT_DESCRIPTION);
            Tuple *part = dict_find(iter,ACHIEVEMENT_NUMBER);
            if(part && text && id) {
                if(current_achievement->id==(int)id->value->uint16) {
                    if((int)part->value->uint16==0)
                        strcpy(current_achievement_description,text->value->cstring);
                    else
                        strcat(current_achievement_description,text->value->cstring);
                }
                //APP_LOG(APP_LOG_LEVEL_DEBUG, "Recieved achievement description: id: %u | part: %u | text: %s",(int)id->value->uint16,(int)part->value->uint16,text->value->cstring);
                if(achievement_details_content_text_layer && achievement_details_scroll_layer) {
                    text_layer_set_text(achievement_details_content_text_layer,current_achievement_description);
                    GSize max_content_size = text_layer_get_content_size(achievement_details_content_text_layer);
                    max_content_size.w = 134;
                    max_content_size.h += 10;
                    text_layer_set_size(achievement_details_content_text_layer,max_content_size);
                    scroll_layer_set_content_size(achievement_details_scroll_layer,GSize(144,max_content_size.h));
                }
            }
        }
        else if(receiving_type->value->uint16==RESPONSE_ACHIEVEMENT_BADGE && user.logged_on && current_achievement) {
            Tuple *offset = dict_find(iter,ACHIEVEMENT_BADGE_OFFSET);
            Tuple *data = dict_find(iter,ACHIEVEMENT_BADGE_DATA);
            if(data && offset) {
                static int part;
                if((int)dict_find(iter,ACHIEVEMENT_ID)->value->uint16==current_achievement->id) {
                    if(offset->value->uint16==0) {
                        part = 0;
                        if(achievement_details_downloading_sign_layer)
                            layer_set_hidden(bitmap_layer_get_layer(achievement_details_downloading_sign_layer),false);
                    }
                    else
                        part++;
                    if(part<6)
                        memcpy(&current_achievement_badge_data[offset->value->uint16],data->value->data,data->length);
                    if(part==5) {
                        badge_loaded = true;
                        if(achievement_details_badge_layer && achievement_details_scroll_layer) {
                            GRect upper_bounds = layer_get_frame(text_layer_get_layer(achievement_details_content_text_layer));
                            layer_set_frame(bitmap_layer_get_layer(achievement_details_badge_layer),GRect(0,upper_bounds.size.h+upper_bounds.origin.y,144,74));
                            layer_mark_dirty(bitmap_layer_get_layer(achievement_details_badge_layer));
                            GSize scroll_layer_size = scroll_layer_get_content_size(achievement_details_scroll_layer);
                            scroll_layer_set_content_size(achievement_details_scroll_layer,GSize(scroll_layer_size.w,upper_bounds.size.h+upper_bounds.origin.y+74));
                            scroll_layer_add_child(achievement_details_scroll_layer,bitmap_layer_get_layer(achievement_details_badge_layer));
                        }
                        if(achievement_details_downloading_sign_layer)
                            layer_set_hidden(bitmap_layer_get_layer(achievement_details_downloading_sign_layer),true);
                    }
                }
                //APP_LOG(APP_LOG_LEVEL_DEBUG, "Received badge: ach.id: %i | part: %i | data length: %i",(int)dict_find(iter,ACHIEVEMENT_ID)->value->uint16,part,data->length);
            }
        }
        else if(receiving_type->value->uint16==RESPONSE_COWORKER_POP && user.logged_on) {
            Tuple *name = dict_find(iter,COWORKER_NAME);
            //APP_LOG(APP_LOG_LEVEL_DEBUG, "Recieved coworker to pop: id: %u | name: %s | from room id: %u | page %i",(int)dict_find(iter,COWORKER_ID)->value->uint16,name->value->cstring,(int)dict_find(iter,COWORKER_BEACON_ID)->value->uint16,(int)dict_find(iter,COWORKER_PAGE)->value->uint16);
            if((int)dict_find(iter,COWORKER_PAGE)->value->uint16+1==current_coworkers_page && (int)dict_find(iter,COWORKER_BEACON_ID)->value->uint16==current_beacon->id && pop_coworker_from_table((int)dict_find(iter,COWORKER_ID)->value->uint16)) {
                if(window_stack_get_top_window()==coworkers_window) {
                    menu_layer_reload_data(coworkers_menu_layer);
                }
            }
            is_downloading = false;
            if(coworkers==NULL) {
                if(coworkers_menu_layer)
                    layer_set_hidden(menu_layer_get_layer(coworkers_menu_layer),true);
                if(coworkers_text_layer)
                    layer_set_hidden(text_layer_get_layer(coworkers_text_layer),false);
            }
        }
        else if(receiving_type->value->uint16==RESPONSE_ACHIEVEMENT_POP && user.logged_on) {
            Tuple *name = dict_find(iter,ACHIEVEMENT_NAME);
            //APP_LOG(APP_LOG_LEVEL_DEBUG, "Recieved achievement to pop: id: %u | name: %s | from page: %i",(int)dict_find(iter,ACHIEVEMENT_ID)->value->uint16,name->value->cstring,(int)dict_find(iter,ACHIEVEMENT_NUMBER)->value->uint16);
            if((int)dict_find(iter,ACHIEVEMENT_NUMBER)->value->uint16+1==current_achievements_page && pop_achievement_from_table((int)dict_find(iter,ACHIEVEMENT_ID)->value->uint16)) {
                if(window_stack_get_top_window()==achievements_window || window_stack_get_top_window()==achievement_details_window) {
                    menu_layer_reload_data(achievements_menu_layer);
                }
            }
            is_downloading = false;
        }
    }
}

void in_dropped_handler(AppMessageResult reason, void *context) {
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
    static char text_buffer[40];
    if(strlen(user.location)>0)
        snprintf(text_buffer,40,"in %s",user.location);
    else
        snprintf(text_buffer,40," ");
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
    if(user.logged_on) {
        if(num_beacons==0 && user.beacons_pages>0) {
            send_query_request(REQUEST_BEACONS_PAGE,1,0);
            current_beacons_page = 1;
            is_downloading = true;
        }
        window_stack_push(beacons_window,animated);
    }
}

static void login_menu_user_callback(int index, void *ctx) {
    if(user.logged_on) {
        window_stack_push(user_window,animated);
    }
}

static void login_request_sending(void *data) {
    if(!user.logged_on) {
        send_simple_request(REQUEST_USER);
        login_timer = app_timer_register(2000,login_request_sending,NULL);
    }
}

static void login_window_load(Window *window) {
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
    
    if(!user.logged_on)
        layer_set_hidden(simple_menu_layer_get_layer(login_menu_layer),true);
}

static void login_window_unload(Window *window) {
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
}
/////////////////////////////////////

///////////////////////////////////// USER WINDOW
static SimpleMenuSection user_menu_sections[1];
static SimpleMenuItem user_menu_first_section_items[1];

static void user_menu_achievements_callback(int index, void *ctx) {
    if((num_achievements==0 && user.achievements_pages>0) || current_achievements_page!=1) {
        clear_achievements_table();
        send_query_request(REQUEST_ACHIEVEMENTS_PAGE,1,0);
        is_downloading = true;
    }
    if(coworkers!=NULL) {
        clear_coworkers_table();
        current_beacon = NULL;
        previous_beacon = NULL;
    }
    current_achievements_page = 1;
    window_stack_push(achievements_window,animated);
}

static void user_window_load(Window *window) {
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
    snprintf(text_buffer1,40," Points:\n Rank:\n Achievements:");
    text_layer_set_text(user_left_text_layer,text_buffer1);
    text_layer_set_font(user_left_text_layer,fonts_get_system_font(FONT_KEY_GOTHIC_24));
    text_layer_set_text_alignment(user_left_text_layer, GTextAlignmentLeft);
    
    GRect right_text_bounds = left_text_bounds;
    right_text_bounds.size.w = right_text_layer_width;
    right_text_bounds.origin.x = 144-right_text_layer_width;
    user_right_text_layer = text_layer_create(right_text_bounds);
    static char text_buffer2[25];
    snprintf(text_buffer2,25,"%i \n%i \n%i ",user.points,user.rank,user.achievements);
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
}

static void user_window_unload(Window *window) {
    simple_menu_layer_destroy(user_menu_layer);
    user_menu_layer = NULL;
    text_layer_destroy(user_right_text_layer);
    user_right_text_layer = NULL;
    text_layer_destroy(user_left_text_layer);
    user_left_text_layer = NULL;
    text_layer_destroy(user_textbar_layer);
    user_textbar_layer = NULL;
}
/////////////////////////////////////

///////////////////////////////////// BEACONS WINDOW
static uint16_t get_num_sections_beacons(MenuLayer *menu_layer, void *data) {
    return 1;
}

static void draw_beacon_header(GContext* ctx, const Layer *cell_layer, uint16_t section_index, void *data) {
    if(section_index==0) {
        GRect header_layer_bounds = layer_get_bounds(cell_layer);
        header_layer_bounds.origin.y -= 1;
        graphics_context_set_text_color(ctx, GColorBlack);
        static char text_buffer[22];
        if(user.beacons_pages>1)
            snprintf(text_buffer,22,"Locations (%i/%i)",current_beacons_page,user.beacons_pages);
        else
            snprintf(text_buffer,22,"Locations");
        graphics_draw_text(ctx, text_buffer, fonts_get_system_font(FONT_KEY_GOTHIC_14_BOLD), header_layer_bounds, GTextOverflowModeFill , GTextAlignmentCenter, NULL);
    }
}

static void draw_beacon_row(GContext *ctx, const Layer *cell_layer, MenuIndex *cell_index, void *callback_context) {
    GRect cell_layer_bounds = layer_get_bounds(cell_layer);
    cell_layer_bounds.size.w -= 10;
    cell_layer_bounds.origin.x += 5;
    cell_layer_bounds.origin.y -= 2;
    graphics_context_set_text_color(ctx, GColorBlack);
    if(current_beacons_page>1 && cell_index->row==0)
        graphics_draw_text(ctx, "...previous page...", fonts_get_system_font(FONT_KEY_GOTHIC_18), cell_layer_bounds, GTextOverflowModeFill , GTextAlignmentCenter, NULL);
    else if(current_beacons_page<user.beacons_pages && cell_index->row==num_beacons+(current_beacons_page==1?0:1))
        graphics_draw_text(ctx, "...next page...", fonts_get_system_font(FONT_KEY_GOTHIC_18), cell_layer_bounds, GTextOverflowModeFill , GTextAlignmentCenter, NULL);
    else if(beacons) {
        if(cell_index->section==0) {
            if(beacons[cell_index->row+(current_beacons_page==1?0:-1)]->name) {
                GRect beacon_name_bounds = cell_layer_bounds;
                beacon_name_bounds.size.w -= 20;
                beacon_name_bounds.origin.x += 5;
                beacon_name_bounds.origin.y -= 2;
                GRect beacon_coworkers_bounds = cell_layer_bounds;
                beacon_coworkers_bounds.size.w = 25;
                beacon_coworkers_bounds.origin.x = beacon_name_bounds.size.w;
                beacon_coworkers_bounds.origin.y -= 2;
                static char text_buffer1[4];
                snprintf(text_buffer1,4,"%i",beacons[cell_index->row+(current_beacons_page==1?0:-1)]->coworkers);
                graphics_draw_text(ctx, beacons[cell_index->row+(current_beacons_page==1?0:-1)]->name, fonts_get_system_font(FONT_KEY_GOTHIC_18), beacon_name_bounds, GTextOverflowModeFill , GTextAlignmentLeft, NULL);
                graphics_draw_text(ctx, text_buffer1, fonts_get_system_font(FONT_KEY_GOTHIC_18), beacon_coworkers_bounds, GTextOverflowModeWordWrap , GTextAlignmentCenter, NULL);
            }
        }
    }
}

static void beacon_select_click(struct MenuLayer *menu_layer, MenuIndex *cell_index, void *callback_context) {
    int new_beacons_page = current_beacons_page;
    if(current_beacons_page==1) {
        if(cell_index->row<num_beacons)
            current_beacon = beacons[cell_index->row];
        else if(cell_index->row==num_beacons) {
            new_beacons_page += 1;
        }
    }
    else if(current_beacons_page<=user.beacons_pages) {
        if(cell_index->row==0) {
            new_beacons_page -= 1;
        }
        else if(cell_index->row<num_beacons+1)
            current_beacon = beacons[cell_index->row-1];
        else if(cell_index->row==num_beacons+1) {
            new_beacons_page += 1;
        }
    }
    if(beacons && new_beacons_page!=current_beacons_page) {
        clear_beacons_table();
        current_beacons_page = new_beacons_page;
        send_query_request(REQUEST_BEACONS_PAGE,new_beacons_page,0);
        is_downloading = true;
        current_beacon = NULL;
        menu_layer_reload_data(beacons_menu_layer);
        layer_set_hidden(bitmap_layer_get_layer(beacons_downloading_sign_layer),false);
    }
    else if(current_beacon) {
        if(previous_beacon_id!=current_beacon->id || (previous_beacon==current_beacon && current_coworkers_page!=1) || (num_coworkers==0 && current_beacon->coworkers>0)) {
            clear_coworkers_table();
            send_query_request(REQUEST_COWORKERS_PAGE,current_beacon->id,1);
            is_downloading = true;
            previous_beacon_id = current_beacon->id;
        }
        previous_beacon = current_beacon;
        if(achievements!=NULL) {
            clear_achievements_table();
            current_achievement = NULL;
            previous_achievement = NULL;
        }
        current_coworkers_page = 1;
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
    Layer *window_layer = window_get_root_layer(window);
    
    set_textbar_layer(window_layer,&beacons_textbar_layer);
    text_layer_set_text(beacons_textbar_layer,user.name);
    
    set_downloading_sign_layer(window_layer,&beacons_downloading_sign_layer);
    layer_set_hidden(bitmap_layer_get_layer(beacons_downloading_sign_layer),(is_downloading && last_request==REQUEST_BEACONS_PAGE)?false:true);
    
    GRect menu_bounds = layer_get_bounds(window_layer);
    menu_bounds.size.h -= textbar_height;
    menu_bounds.origin.y += textbar_height;
    beacons_menu_layer = menu_layer_create(menu_bounds);
    menu_layer_set_callbacks(beacons_menu_layer, NULL, beacons_menu_callbacks);
    menu_layer_set_click_config_onto_window(beacons_menu_layer, window);
    
    layer_add_child(window_layer, text_layer_get_layer(beacons_textbar_layer));
    layer_add_child(window_layer, bitmap_layer_get_layer(beacons_downloading_sign_layer));
    layer_add_child(window_layer, menu_layer_get_layer(beacons_menu_layer));
}

static void beacons_window_unload(Window *window) {
    menu_layer_destroy(beacons_menu_layer);
    beacons_menu_layer = NULL;
    bitmap_layer_destroy(beacons_downloading_sign_layer);
    beacons_downloading_sign_layer = NULL;
    text_layer_destroy(beacons_textbar_layer);
    beacons_textbar_layer = NULL;
}
/////////////////////////////////////

///////////////////////////////////// COWORKERS WINDOW
static uint16_t get_num_sections_coworkers(MenuLayer *menu_layer, void *data) {
    return 1;
}

static void draw_coworker_header(GContext* ctx, const Layer *cell_layer, uint16_t section_index, void *data) {
    if(section_index==0) {
        GRect header_layer_bounds = layer_get_bounds(cell_layer);
        header_layer_bounds.origin.y -= 1;
        graphics_context_set_text_color(ctx, GColorBlack);
        static char text_buffer[27];
        if(current_beacon->coworkers_pages>1)
            snprintf(text_buffer,27,"People im room (%i/%i)",current_coworkers_page,current_beacon->coworkers_pages);
        else
            snprintf(text_buffer,27,"People im room");
        graphics_draw_text(ctx, text_buffer, fonts_get_system_font(FONT_KEY_GOTHIC_14_BOLD), header_layer_bounds, GTextOverflowModeFill , GTextAlignmentCenter, NULL);
    }
}

static void draw_coworker_row(GContext *ctx, const Layer *cell_layer, MenuIndex *cell_index, void *callback_context) {
    GRect cell_layer_bounds = layer_get_bounds(cell_layer);
    cell_layer_bounds.size.w -= 10;
    cell_layer_bounds.origin.x += 5;
    cell_layer_bounds.origin.y -= 2;
    graphics_context_set_text_color(ctx, GColorBlack);
    if(current_coworkers_page>1 && cell_index->row==0)
        graphics_draw_text(ctx, "...previous page...", fonts_get_system_font(FONT_KEY_GOTHIC_18), cell_layer_bounds, GTextOverflowModeFill , GTextAlignmentCenter, NULL);
    else if(current_coworkers_page<current_beacon->coworkers_pages && cell_index->row==num_coworkers+(current_coworkers_page==1?0:1))
        graphics_draw_text(ctx, "...next page...", fonts_get_system_font(FONT_KEY_GOTHIC_18), cell_layer_bounds, GTextOverflowModeFill , GTextAlignmentCenter, NULL);
    else if(coworkers) {
        if(cell_index->section==0) {
            if(coworkers[cell_index->row+(current_coworkers_page==1?0:-1)]->name) {
                graphics_draw_text(ctx, coworkers[cell_index->row+(current_coworkers_page==1?0:-1)]->name, fonts_get_system_font(FONT_KEY_GOTHIC_18), cell_layer_bounds, GTextOverflowModeFill , GTextAlignmentLeft, NULL);
            }
        }
    }
}

static void coworker_select_click(struct MenuLayer *menu_layer, MenuIndex *cell_index, void *callback_context) {
    int new_coworkers_page = current_coworkers_page;
    if(current_coworkers_page==1) {
        if(cell_index->row<num_coworkers)
            ;//current_coworker = coworkers[cell_index->row];
        else if(cell_index->row==num_coworkers) {
            new_coworkers_page += 1;
        }
    }
    else if(current_coworkers_page<=current_beacon->coworkers_pages) {
        if(cell_index->row==0) {
            new_coworkers_page -= 1;
        }
        else if(cell_index->row<num_coworkers+1)
            ;//current_coworker = coworkers[cell_index->row-1];
        else if(cell_index->row==num_coworkers+1) {
            new_coworkers_page += 1;
        }
    }
    if(coworkers && new_coworkers_page!=current_coworkers_page) {
        clear_coworkers_table();
        current_coworkers_page = new_coworkers_page;
        send_query_request(REQUEST_COWORKERS_PAGE,current_beacon->id,new_coworkers_page);
        is_downloading = true;
        menu_layer_reload_data(coworkers_menu_layer);
        layer_set_hidden(bitmap_layer_get_layer(coworkers_downloading_sign_layer),false);
    }
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
    Layer *window_layer = window_get_root_layer(window);
    
    set_textbar_layer(window_layer,&coworkers_textbar_layer);
    text_layer_set_text(coworkers_textbar_layer,current_beacon->name);
    
    set_downloading_sign_layer(window_layer,&coworkers_downloading_sign_layer);
    layer_set_hidden(bitmap_layer_get_layer(coworkers_downloading_sign_layer),(is_downloading && last_request==REQUEST_COWORKERS_PAGE && current_beacon->coworkers>0)?false:true);
    
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
}

static void coworkers_window_unload(Window *window) {
    text_layer_destroy(coworkers_text_layer);
    coworkers_text_layer = NULL;
    menu_layer_destroy(coworkers_menu_layer);
    coworkers_menu_layer = NULL;
    bitmap_layer_destroy(coworkers_downloading_sign_layer);
    coworkers_downloading_sign_layer = NULL;
    text_layer_destroy(coworkers_textbar_layer);
    coworkers_textbar_layer = NULL;
}
/////////////////////////////////////

///////////////////////////////////// ACHIEVEMENTS WINDOW
static uint16_t get_num_sections_achievements(MenuLayer *menu_layer, void *data) {
    return 1;
}

static void draw_achievement_header(GContext* ctx, const Layer *cell_layer, uint16_t section_index, void *data) {
    if(section_index==0) {
        GRect header_layer_bounds = layer_get_bounds(cell_layer);
        header_layer_bounds.origin.y -= 1;
        graphics_context_set_text_color(ctx, GColorBlack);
        static char text_buffer[25];
        if(user.achievements_pages>1)
            snprintf(text_buffer,25,"Achievements (%i/%i)",current_achievements_page,user.achievements_pages);
        else
            snprintf(text_buffer,25,"Achievements");
        graphics_draw_text(ctx, text_buffer, fonts_get_system_font(FONT_KEY_GOTHIC_14_BOLD), header_layer_bounds, GTextOverflowModeFill , GTextAlignmentCenter, NULL);
    }
}

static void draw_achievement_row(GContext *ctx, const Layer *cell_layer, MenuIndex *cell_index, void *callback_context) {
    GRect cell_layer_bounds = layer_get_bounds(cell_layer);
    cell_layer_bounds.size.w -= 10;
    cell_layer_bounds.origin.x += 5;
    cell_layer_bounds.origin.y -= 2;
    graphics_context_set_text_color(ctx, GColorBlack);
    if(current_achievements_page>1 && cell_index->row==0)
        graphics_draw_text(ctx, "...previous page...", fonts_get_system_font(FONT_KEY_GOTHIC_18), cell_layer_bounds, GTextOverflowModeFill, GTextAlignmentCenter, NULL);
    else if(current_achievements_page<user.achievements_pages && cell_index->row==num_achievements+(current_achievements_page==1?0:1))
        graphics_draw_text(ctx, "...next page...", fonts_get_system_font(FONT_KEY_GOTHIC_18), cell_layer_bounds, GTextOverflowModeFill, GTextAlignmentCenter, NULL);
    else if(achievements) {
        if(cell_index->section==0) {
            if(achievements[cell_index->row+(current_achievements_page==1?0:-1)]->name) {
                graphics_draw_text(ctx, achievements[cell_index->row+(current_achievements_page==1?0:-1)]->name, fonts_get_system_font(FONT_KEY_GOTHIC_18), cell_layer_bounds, GTextOverflowModeFill, GTextAlignmentLeft, NULL);
            }
        }
    }
}

static void achievement_select_click(struct MenuLayer *menu_layer, MenuIndex *cell_index, void *callback_context) {
    int new_achievements_page = current_achievements_page;
    if(current_achievements_page==1) {
        if(cell_index->row<num_achievements)
            current_achievement = achievements[cell_index->row];
        else if(cell_index->row==num_achievements) {
            new_achievements_page += 1;
        }
    }
    else if(current_achievements_page<=user.achievements_pages) {
        if(cell_index->row==0) {
            new_achievements_page -= 1;
        }
        else if(cell_index->row<num_achievements+1)
            current_achievement = achievements[cell_index->row-1];
        else if(cell_index->row==num_achievements+1) {
            new_achievements_page += 1;
        }
    }
    if(achievements && new_achievements_page!=current_achievements_page) {
        clear_achievements_table();
        current_achievements_page = new_achievements_page;
        send_query_request(REQUEST_ACHIEVEMENTS_PAGE,new_achievements_page,0);
        is_downloading = true;
        current_achievement = NULL;
        menu_layer_reload_data(achievements_menu_layer);
        layer_set_hidden(bitmap_layer_get_layer(achievements_downloading_sign_layer),false);
    }
    else if(current_achievement) {
        if(previous_achievement_id!=current_achievement->id) {
            current_achievement_description[0] = '\0';
            send_query_request(REQUEST_ACHIEVEMENT_CONTENT,current_achievement->id,0);
            is_downloading = true;
            badge_loaded = false;
        }
 		previous_achievement_id = current_achievement->id;
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
    Layer *window_layer = window_get_root_layer(window);
    
    set_textbar_layer(window_layer,&achievements_textbar_layer);
    text_layer_set_text(achievements_textbar_layer,user.name);
    
    set_downloading_sign_layer(window_layer,&achievements_downloading_sign_layer);
    layer_set_hidden(bitmap_layer_get_layer(achievements_downloading_sign_layer),(is_downloading && user.achievements!=0 && last_request==REQUEST_ACHIEVEMENTS_PAGE)?false:true);
    
    GRect menu_bounds = layer_get_bounds(window_layer);
    menu_bounds.size.h -= textbar_height;
    menu_bounds.origin.y += textbar_height;
    achievements_menu_layer = menu_layer_create(menu_bounds);
    menu_layer_set_callbacks(achievements_menu_layer, NULL, achievements_menu_callbacks);
    menu_layer_set_click_config_onto_window(achievements_menu_layer, window);
    
    achievements_text_layer = text_layer_create(menu_bounds);
    text_layer_set_text(achievements_text_layer,"You have no achievements");
    text_layer_set_font(achievements_text_layer,fonts_get_system_font(FONT_KEY_GOTHIC_18));
    text_layer_set_text_alignment(achievements_text_layer, GTextAlignmentCenter);
    
    if(user.achievements!=0) {
        layer_set_hidden(text_layer_get_layer(achievements_text_layer),true);
        layer_set_hidden(menu_layer_get_layer(achievements_menu_layer),false);
    }
    else {
        layer_set_hidden(menu_layer_get_layer(achievements_menu_layer),true);
        layer_set_hidden(text_layer_get_layer(achievements_text_layer),false);
    }
    
    layer_add_child(window_layer, text_layer_get_layer(achievements_textbar_layer));
    layer_add_child(window_layer, bitmap_layer_get_layer(achievements_downloading_sign_layer));
    layer_add_child(window_layer, menu_layer_get_layer(achievements_menu_layer));
    layer_add_child(window_layer, text_layer_get_layer(achievements_text_layer));
}

static void achievements_window_unload(Window *window) {
    text_layer_destroy(achievements_text_layer);
    achievements_text_layer = NULL;
    menu_layer_destroy(achievements_menu_layer);
    achievements_menu_layer = NULL;
    bitmap_layer_destroy(achievements_downloading_sign_layer);
    achievements_downloading_sign_layer = NULL;
    text_layer_destroy(achievements_textbar_layer);
    achievements_textbar_layer = NULL;
}

static void achievements_window_appear(Window *window) {
    menu_layer_reload_data(achievements_menu_layer);
}
/////////////////////////////////////

///////////////////////////////////// ACHIEVEMENT DETAILS WINDOW
static void achievement_details_window_load(Window *window) {
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
    max_title_size.h += 5;
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
    
    current_achievement_badge_bitmap = (GBitmap) {
        .addr = current_achievement_badge_data,
        .bounds = GRect(0,0,64,64),
        .info_flags = 1,
        .row_size_bytes = 8
    };
    GRect badge_bounds = GRect(0,max_title_size.h+max_content_size.h,144,64+10);
    achievement_details_badge_layer = bitmap_layer_create(badge_bounds);
    bitmap_layer_set_alignment(achievement_details_badge_layer,GAlignCenter);
    bitmap_layer_set_bitmap(achievement_details_badge_layer,&current_achievement_badge_bitmap);
    
    GRect scroll_layer_bounds = layer_get_bounds(window_layer);
    scroll_layer_bounds.size.h -= textbar_height;
    scroll_layer_bounds.origin.y = textbar_height;
    achievement_details_scroll_layer = scroll_layer_create(scroll_layer_bounds);
    scroll_layer_set_click_config_onto_window(achievement_details_scroll_layer,achievement_details_window);
    
    scroll_layer_set_content_size(achievement_details_scroll_layer,GSize(max_text_bounds.size.w,max_title_size.h+max_content_size.h));
    scroll_layer_add_child(achievement_details_scroll_layer,text_layer_get_layer(achievement_details_title_text_layer));
    scroll_layer_add_child(achievement_details_scroll_layer,text_layer_get_layer(achievement_details_content_text_layer));
    if(badge_loaded) {
        scroll_layer_add_child(achievement_details_scroll_layer,bitmap_layer_get_layer(achievement_details_badge_layer));
        scroll_layer_set_content_size(achievement_details_scroll_layer,GSize(max_text_bounds.size.w,max_title_size.h+max_content_size.h+74));
    }
    
    layer_add_child(window_layer, text_layer_get_layer(achievement_details_textbar_layer));
    layer_add_child(window_layer, bitmap_layer_get_layer(achievement_details_downloading_sign_layer));
    layer_add_child(window_layer, scroll_layer_get_layer(achievement_details_scroll_layer));
}

static void achievement_details_window_unload(Window *window) {
    bitmap_layer_destroy(achievement_details_badge_layer);
    achievement_details_badge_layer = NULL;
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
    .unload = achievements_window_unload,
    .appear = achievements_window_appear
};
static WindowHandlers achievement_details_window_handlers = {
    .load = achievement_details_window_load,
    .unload = achievement_details_window_unload
};

static void init() {
    previous_achievement_id = -1;
    previous_beacon_id = -1;
    
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
    const int outbound_size = 32;
    app_message_open(inbound_size,outbound_size);
    
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