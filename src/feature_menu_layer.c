///    @author: Pawel Palka
///    @email: ppalka@sosoftware.pl    // company
///    @email: fractalord@gmail.com    // private

#include "pebble.h"

/////////////////////////////////////
static Window *login_window;
static Window *user_window;
static Window *beacons_window;
static Window *beacon_details_window;
static Window *games_window;
static Window *game_details_window;
static Window *waiting_window;
static SimpleMenuLayer *login_menu_layer;
static MenuLayer *achievements_menu_layer;
static MenuLayer *beacons_menu_layer;
static MenuLayer *games_menu_layer;
static TextLayer *login_uppertext_layer;
static TextLayer *login_lowertext_layer;
static TextLayer *user_textbar_layer;
static TextLayer *user_text_layer;
static TextLayer *beacons_textbar_layer;
static TextLayer *beacon_details_textbar_layer;
static TextLayer *beacon_details_uppertext_layer;
static TextLayer *beacon_details_lowertext_layer;
static TextLayer *games_textbar_layer;
static TextLayer *game_details_textbar_layer;
static TextLayer *game_details_text_layer;
static TextLayer *waiting_textbar_layer;
static TextLayer *waiting_text_layer;
static InverterLayer *user_textbar_inverter_layer;
static InverterLayer *beacons_textbar_inverter_layer;
static InverterLayer *beacon_details_textbar_inverter_layer;
static InverterLayer *games_textbar_inverter_layer;
static InverterLayer *game_details_textbar_inverter_layer;
static InverterLayer *waiting_textbar_inverter_layer;
static Layer *beacon_details_distance_layer;

static AppTimer *timer;
static char *current_user_name;
static char *current_beacon_name;
static char *current_game_name;
static char *current_game_description;
static char *current_achievement_name;
static uint8_t current_beacon_distance;
static uint8_t current_beacon_active_games;
static uint8_t current_beacon_completed_games;
static uint8_t waiting_for_info;
static uint16_t current_user_points;
static uint16_t current_user_rank;

enum {
    WAITING_FOR_USER_DETAILS = 1,
    WAITING_FOR_BEACONS = 2,
    WAITING_FOR_BEACON_DETAILS = 3,
    WAITING_FOR_GAMES = 4,
    WAITING_FOR_GAME_DETAILS = 5,
    WAITING_FOR_ACHIEVEMENT_DETAILS = 6
};
/////////////////////////////////////

///////////////////////////////////// USERS INITIALISING
/*
typedef struct {
    char *name;
    uint16_t points;
} User;
 */
/////////////////////////////////////

///////////////////////////////////// ACHIEVEMENTS INITIALISING
typedef struct {
    char *name;
} Achievement;

Achievement *achievements = NULL;
static int achievements_amount = 0;

static uint16_t get_num_achievements(struct MenuLayer* menu_layer, uint16_t section_index, void *callback_context) {
    return achievements_amount;
}
/////////////////////////////////////

///////////////////////////////////// BEACONS INITIALISING
typedef struct {
    char *name;
    //uint16_t uuid;
} Beacon;

Beacon *beacons_in_range = NULL;
Beacon *beacons_out_of_range = NULL;
static int beacons_in_range_amount = 0;
static int beacons_out_of_range_amount = 0;

static uint16_t get_num_beacons(struct MenuLayer* menu_layer, uint16_t section_index, void *callback_context) {
    uint16_t num = 0;
    if(section_index==0)
        num = beacons_in_range_amount;
    else if(section_index==1)
        num = beacons_out_of_range_amount;
    return num;
}
/////////////////////////////////////

///////////////////////////////////// GAMES INITIALISING
typedef struct {
    char *name;
    //uint16_t uuid;
} Game;

Game *games_active = NULL;
Game *games_completed = NULL;
static int games_active_amount = 0;
static int games_completed_amount = 0;

static uint16_t get_num_games(struct MenuLayer* menu_layer, uint16_t section_index, void *callback_context) {
    uint16_t num = 0;
    if(section_index==0)
        num = games_active_amount;
    else if(section_index==1)
        num = games_completed_amount;
    return num;
}
/////////////////////////////////////

///////////////////////////////////// COMMUNICATION
enum { // actualise, not every position is needed
    REQUEST = 1,
    REQUEST_QUERY = 2,
    REQUEST_USER_DETAILS = 11,
    REQUEST_BEACONS_IN_RANGE = 12,
    REQUEST_BEACONS_OUT_OF_RANGE = 13,
    REQUEST_GAMES_ACTIVE = 14,
    REQUEST_GAMES_COMPLETED = 15,
    REQUEST_USER = 16,
    REQUEST_BEACON_DETAILS = 17,
    REQUEST_PROGRESS = 18,
    REQUEST_GAME_DETAILS = 19,
    REQUEST_ACHIEVEMENT_DETAILS = 20, 
    RESPONSE_TYPE = 200,
    RESPONSE_LENGTH = 201,
    RESPONSE_USER_DETAILS = 21,
    RESPONSE_BEACONS_IN_RANGE = 22,
    RESPONSE_BEACONS_OUT_OF_RANGE = 23,
    RESPONSE_GAMES_ACTIVE = 24,
    RESPONSE_GAMES_COMPLETED = 25,
    RESPONSE_USER = 26,
    RESPONSE_BEACON_DETAILS = 27,
    RESPONSE_PROGRESS = 28,
    RESPONSE_GAME_DETAILS = 29,
    RESPONSE_ACHIEVEMENT_DETAILS = 30
};

static void send_simple_request(int8_t request) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "Sending function without a parameter. Request: %u",request);
    DictionaryIterator *iter;
    app_message_outbox_begin(&iter);
    
    Tuplet value = TupletInteger(REQUEST,request);
    dict_write_tuplet(iter,&value);
    dict_write_end(iter);
    
    app_message_outbox_send();
}

static void send_query_request(int8_t request, char *query) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "Sending function with a parameter. Request: %u Query: %s",request,query);
    DictionaryIterator *iter;
    app_message_outbox_begin(&iter);
    
    Tuplet value = TupletInteger(REQUEST,request);
    Tuplet value2 = TupletCString(REQUEST_QUERY,query);
    dict_write_tuplet(iter,&value);
    dict_write_tuplet(iter,&value2);
    dict_write_end(iter);
    
    app_message_outbox_send();
}

void out_sent_handler(DictionaryIterator *sent, void *context) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "Dictionary sent");
}

void out_failed_handler(DictionaryIterator *failed, AppMessageResult reason, void *context) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "Dictionary not sent! Reason number: %u", reason);
}

void in_received_handler(DictionaryIterator *iter, void *context) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "Receiving in progress...");
    Tuple *receiving_type = dict_find(iter,RESPONSE_TYPE);
    Tuple *receiving_amount = dict_find(iter,RESPONSE_LENGTH);
    
    if(receiving_type&&receiving_amount) {
        int amount = 0;
        amount = receiving_amount->value->data[0];
        APP_LOG(APP_LOG_LEVEL_DEBUG, "Response Type: %u",receiving_type->value->data[0]);
        if(receiving_type->value->data[0]==RESPONSE_BEACONS_IN_RANGE) {
            if(beacons_in_range!=NULL && beacons_in_range_amount!=0) {
                int i;
                for(i=0; i<beacons_in_range_amount; ++i)
                    free(beacons_in_range[i].name);
                free(beacons_in_range);
            }
            beacons_in_range = (Beacon*)calloc(amount,sizeof(Beacon));
            int i;
            for(i=0; i<amount; ++i) {
                Tuple *tuple = dict_find(iter,i);
                char *new = (char*)calloc(strlen(tuple->value->cstring),sizeof(char));
                strcpy(new,tuple->value->cstring);
                beacons_in_range[i].name = new;
                APP_LOG(APP_LOG_LEVEL_DEBUG, "Adding new beacon in range: %u - %s",i,beacons_in_range[i].name);
            }
            beacons_in_range_amount = amount;
            send_simple_request(REQUEST_BEACONS_OUT_OF_RANGE);
        }
        else if(receiving_type->value->data[0]==RESPONSE_BEACONS_OUT_OF_RANGE) {
            if(beacons_out_of_range!=NULL && beacons_out_of_range_amount!=0) {
                int i;
                for(i=0; i<beacons_out_of_range_amount; ++i)
                    free(beacons_out_of_range[i].name);
                free(beacons_out_of_range);
            }
            beacons_out_of_range = (Beacon*)calloc(amount,sizeof(Beacon));
            int i;
            for(i=0; i<amount; ++i) {
                Tuple *tuple = dict_find(iter,i);
                char *new = (char*)calloc(strlen(tuple->value->cstring),sizeof(char));
                strcpy(new,tuple->value->cstring);
                beacons_out_of_range[i].name = new;
                APP_LOG(APP_LOG_LEVEL_DEBUG, "Adding new beacon out of range: %u - %s",i,beacons_out_of_range[i].name);
            }
            beacons_out_of_range_amount = amount;
            APP_LOG(APP_LOG_LEVEL_DEBUG, "------------ 1");
            window_stack_push(beacons_window, true);
            APP_LOG(APP_LOG_LEVEL_DEBUG, "------------ 2");
            window_stack_remove(waiting_window, false);
            APP_LOG(APP_LOG_LEVEL_DEBUG, "------------ 3");
        }
        else if(receiving_type->value->data[0]==RESPONSE_BEACON_DETAILS) {
            Tuple *tuple1 = dict_find(iter,0);
            current_beacon_distance = tuple1->value->data[0];
            if(current_beacon_distance>100)
                current_beacon_distance = 100;
            APP_LOG(APP_LOG_LEVEL_DEBUG, "Receiving beacon distance: %u",current_beacon_distance);
            Tuple *tuple2 = dict_find(iter,1);
            current_beacon_active_games = tuple2->value->data[0];
            APP_LOG(APP_LOG_LEVEL_DEBUG, "Receiving beacon active games: %u",current_beacon_active_games);
            Tuple *tuple3 = dict_find(iter,2);
            current_beacon_completed_games = tuple3->value->data[0];
            APP_LOG(APP_LOG_LEVEL_DEBUG, "Receiving beacon completed games: %u",current_beacon_completed_games);
            window_stack_push(beacon_details_window, true);
            window_stack_remove(waiting_window, false);
        }
        else if(receiving_type->value->data[0]==RESPONSE_USER && current_user_name==NULL) {
            Tuple *tuple = dict_find(iter,0);
            char *new = (char*)calloc(strlen(tuple->value->cstring),sizeof(char));
            strcpy(new,tuple->value->cstring);
            current_user_name = new;
            tuple = dict_find(iter,1);
            if(tuple)
                current_user_points = tuple->value->data[0];
            APP_LOG(APP_LOG_LEVEL_DEBUG, "User received: %s | %u",current_user_name,current_user_points);
            static char text_buffer[40];
            snprintf(text_buffer,40,"%s",current_user_name);
            text_layer_set_text(login_lowertext_layer,text_buffer);
            layer_set_hidden(simple_menu_layer_get_layer(login_menu_layer),false);
        }
        else if(receiving_type->value->data[0]==RESPONSE_GAMES_ACTIVE) {
            if(games_active!=NULL) {
                int i;
                for(i=0; i<games_active_amount; ++i)
                    free(games_active[i].name);
                free(games_active);
            }
            games_active = (Game*)calloc(amount,sizeof(Game));
            int i;
            for(i=0; i<amount; ++i) {
                Tuple *tuple = dict_find(iter,i);
                char *new = (char*)calloc(strlen(tuple->value->cstring),sizeof(char));
                strcpy(new,tuple->value->cstring);
                games_active[i].name = new;
                APP_LOG(APP_LOG_LEVEL_DEBUG, "Adding new active game: %u - %s",i,games_active[i].name);
            }
            games_active_amount = amount;
            send_simple_request(REQUEST_GAMES_COMPLETED);
        }
        else if(receiving_type->value->data[0]==RESPONSE_GAMES_COMPLETED) {
            if(games_completed!=NULL) {
                int i;
                for(i=0; i<games_completed_amount; ++i)
                    free(games_completed[i].name);
                free(games_completed);
            }
            games_completed = (Game*)calloc(amount,sizeof(Game));
            int i;
            for(i=0; i<amount; ++i) {
                Tuple *tuple = dict_find(iter,i);
                char *new = (char*)calloc(strlen(tuple->value->cstring),sizeof(char));
                strcpy(new,tuple->value->cstring);
                games_completed[i].name = new;
                APP_LOG(APP_LOG_LEVEL_DEBUG, "Adding new completed game: %u - %s",i,games_completed[i].name);
            }
            games_completed_amount = amount;
            window_stack_push(games_window, true);
            window_stack_remove(waiting_window, false);
        }
        else if(receiving_type->value->data[0]==RESPONSE_GAME_DETAILS) {
            Tuple *tuple = dict_find(iter,0);
            if(current_game_description!=NULL)
                free(current_game_description);
            char *new = (char*)calloc(strlen(tuple->value->cstring),sizeof(char));
            strcpy(new,tuple->value->cstring);
            current_game_description = new;
            APP_LOG(APP_LOG_LEVEL_DEBUG, "Receiving game description: %s",current_game_description);
            window_stack_push(game_details_window, true);
            window_stack_remove(waiting_window, false);
        }
        else if(receiving_type->value->data[0]==RESPONSE_USER_DETAILS) {
            Tuple *tuple = dict_find(iter,0);
            current_user_rank = tuple->value->data[0];
            tuple = dict_find(iter,1);
            current_user_points = tuple->value->data[0];
            APP_LOG(APP_LOG_LEVEL_DEBUG, "Receiving user details: %u | %u",current_user_points,current_user_rank);
            APP_LOG(APP_LOG_LEVEL_DEBUG, "USER------------ 1");
            
            if(achievements!=NULL && achievements_amount!=0) {
                int i;
                for(i=0; i<achievements_amount; ++i)
                    free(achievements[i].name);
                free(achievements);
            }
            achievements = (Achievement*)calloc(amount,sizeof(Achievement));
            int i;
            for(i=2; i<amount; ++i) {
                Tuple *tuple = dict_find(iter,i);
                char *new = (char*)calloc(strlen(tuple->value->cstring),sizeof(char));
                strcpy(new,tuple->value->cstring);
                achievements[i-2].name = new;
                APP_LOG(APP_LOG_LEVEL_DEBUG, "Adding new achievement: %u - %s",i-2,achievements[i-2].name);
            }
            achievements_amount = amount-2;
            
            window_stack_push(user_window, true);
            APP_LOG(APP_LOG_LEVEL_DEBUG, "USER------------ 2");
            window_stack_remove(waiting_window, false);
            APP_LOG(APP_LOG_LEVEL_DEBUG, "USER------------ 3");
        }
    }
    else {
        APP_LOG(APP_LOG_LEVEL_DEBUG, "Receiving error.");
    }
    APP_LOG(APP_LOG_LEVEL_DEBUG, "Receiving done.");
}

void in_dropped_handler(AppMessageResult reason, void *context) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "Receiving rejected. Reason number: %u", reason);
}
/////////////////////////////////////

///////////////////////////////////// LOGIN WINDOW
static SimpleMenuSection login_menu_sections[1];
static SimpleMenuItem login_menu_first_section_items[2];

static void login_menu_beacons_callback(int index, void *ctx) {
    send_simple_request(REQUEST_BEACONS_IN_RANGE);
    waiting_for_info = WAITING_FOR_BEACONS; // change these to dedicated enums everywhere
    window_stack_push(waiting_window, false);
}

static void login_menu_user_callback(int index, void *ctx) {
    send_query_request(REQUEST_USER_DETAILS,current_user_name);
    waiting_for_info = WAITING_FOR_USER_DETAILS;
    window_stack_push(waiting_window, false);
}

static void login_request_sending(void *data) {
    if(current_user_name==NULL) {
        send_simple_request(REQUEST_USER);
        timer = app_timer_register(2000,login_request_sending,NULL);
    }
}

static void login_window_load(Window *window) {
    Layer *window_layer = window_get_root_layer(window);
    int uppertext_height = 40;
    int lowertext_height = 39;
    
    timer = app_timer_register(2000,login_request_sending,NULL);
    send_simple_request(REQUEST_USER);
    
    GRect uppertext_bounds = layer_get_bounds(window_layer);
    uppertext_bounds.size.h = uppertext_height;
    login_uppertext_layer = text_layer_create(uppertext_bounds);
    text_layer_set_text(login_uppertext_layer,"SoI Beacons");
    text_layer_set_font(login_uppertext_layer,fonts_get_system_font(FONT_KEY_GOTHIC_28_BOLD));
    text_layer_set_text_alignment(login_uppertext_layer, GTextAlignmentCenter);
    
    GRect lowertext_bounds = layer_get_bounds(window_layer);
    lowertext_bounds.size.h = lowertext_height;
    lowertext_bounds.origin.y += uppertext_height;
    login_lowertext_layer = text_layer_create(lowertext_bounds);
    text_layer_set_text(login_lowertext_layer,"connecting...");
    text_layer_set_font(login_lowertext_layer,fonts_get_system_font(FONT_KEY_GOTHIC_24));
    text_layer_set_text_alignment(login_lowertext_layer, GTextAlignmentCenter);
    
    login_menu_first_section_items[0] = (SimpleMenuItem) {
        .title = "Beacons list",
        .callback = login_menu_beacons_callback
    };
    login_menu_first_section_items[1] = (SimpleMenuItem) {
        .title = "User details",
        .callback = login_menu_user_callback
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
    
    if(current_user_name==NULL)
        layer_set_hidden(simple_menu_layer_get_layer(login_menu_layer),true);
}

static void login_window_unload(Window *window) {
    text_layer_destroy(login_uppertext_layer);
    text_layer_destroy(login_lowertext_layer);
    simple_menu_layer_destroy(login_menu_layer);
}
/////////////////////////////////////

///////////////////////////////////// USER WINDOW
static uint16_t get_num_sections_achievements(MenuLayer *menu_layer, void *data) {
    return 1;
}

static int16_t get_header_height_achievements(MenuLayer *menu_layer, uint16_t section_index, void *data) {
    return MENU_CELL_BASIC_HEADER_HEIGHT;
}

static int16_t get_cell_height_achievements(MenuLayer *menu_layer, MenuIndex *cell_index, void *data) {
    return 26;
}

static void draw_achievement_header(GContext* ctx, const Layer *cell_layer, uint16_t section_index, void *data) {
    switch (section_index) {
        case 0:
            menu_cell_basic_header_draw(ctx, cell_layer, "Achievements");
            break;
    }
}

static void draw_achievement_row(GContext *ctx, const Layer *cell_layer, MenuIndex *cell_index, void *callback_context) {
    switch (cell_index->section) {
        case 0:
            if(achievements!=NULL)
                menu_cell_basic_draw(ctx, cell_layer, achievements[cell_index->row].name, NULL, NULL);
            break;
    }
}

static void achievement_select_click(struct MenuLayer *menu_layer, MenuIndex *cell_index, void *callback_context) {
    char *new;
    if(cell_index->section==0 && achievements!=NULL) {
        if(current_achievement_name!=NULL)
            free(current_achievement_name);
        new = (char*)calloc(strlen(achievements[cell_index->row].name),sizeof(char));
        strcpy(new,achievements[cell_index->row].name);
        current_achievement_name = new;
    }
    
    send_query_request(REQUEST_ACHIEVEMENT_DETAILS,current_achievement_name);
    
    waiting_for_info = WAITING_FOR_ACHIEVEMENT_DETAILS;
    window_stack_push(waiting_window, false);
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

static void user_window_load(Window *window) {
    Layer *window_layer = window_get_root_layer(window);
    int textbar_height = 18;
    int text_layer_height = 52;
    APP_LOG(APP_LOG_LEVEL_DEBUG, "USER-------- 1");
    GRect textbar_bounds = layer_get_bounds(window_layer);
    APP_LOG(APP_LOG_LEVEL_DEBUG, "USER-------- 2");
    textbar_bounds.size.h = textbar_height;
    APP_LOG(APP_LOG_LEVEL_DEBUG, "USER-------- 3");
    user_textbar_layer = text_layer_create(textbar_bounds);
    APP_LOG(APP_LOG_LEVEL_DEBUG, "USER-------- 4");
    static char text_buffer1[30];
    APP_LOG(APP_LOG_LEVEL_DEBUG, "USER-------- 5");
    snprintf(text_buffer1,30,"%s",current_user_name);
    APP_LOG(APP_LOG_LEVEL_DEBUG, "USER-------- 6");
    text_layer_set_text(user_textbar_layer,text_buffer1);
    APP_LOG(APP_LOG_LEVEL_DEBUG, "USER-------- 7");
    text_layer_set_font(user_textbar_layer,fonts_get_system_font(FONT_KEY_GOTHIC_14));
    APP_LOG(APP_LOG_LEVEL_DEBUG, "USER-------- 8");
    text_layer_set_text_alignment(user_textbar_layer, GTextAlignmentCenter);
    
    user_textbar_inverter_layer = inverter_layer_create(textbar_bounds);
    
    GRect text_bounds = layer_get_bounds(window_layer);
    text_bounds.size.h = text_layer_height;
    text_bounds.origin.y += textbar_height;
    user_text_layer = text_layer_create(text_bounds);
    static char text_buffer2[40];
    snprintf(text_buffer2,40," Points: %u\n Rank: %u",current_user_points,current_user_rank);
    text_layer_set_text(user_text_layer,text_buffer2);
    text_layer_set_font(user_text_layer,fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD));
    text_layer_set_text_alignment(user_text_layer, GTextAlignmentLeft);
    
    GRect menu_bounds = layer_get_bounds(window_layer);
    menu_bounds.size.h -= textbar_height + text_layer_height;
    menu_bounds.origin.y += textbar_height + text_layer_height;
    achievements_menu_layer = menu_layer_create(menu_bounds);
    menu_layer_set_callbacks(achievements_menu_layer, NULL, achievements_menu_callbacks);
    menu_layer_set_click_config_onto_window(achievements_menu_layer, window);
    
    layer_add_child(window_layer, text_layer_get_layer(user_textbar_layer));
    layer_add_child(window_layer, inverter_layer_get_layer(user_textbar_inverter_layer));
    layer_add_child(window_layer, text_layer_get_layer(user_text_layer));
    layer_add_child(window_layer, menu_layer_get_layer(achievements_menu_layer));
}

static void user_window_unload(Window *window) {
    text_layer_destroy(user_textbar_layer);
    inverter_layer_destroy(user_textbar_inverter_layer);
    text_layer_destroy(user_text_layer);
    menu_layer_destroy(achievements_menu_layer);
}
/////////////////////////////////////

///////////////////////////////////// BEACONS WINDOW
static uint16_t get_num_sections_beacons(MenuLayer *menu_layer, void *data) {
    return 2;
}

static int16_t get_header_height_beacons(MenuLayer *menu_layer, uint16_t section_index, void *data) {
    return MENU_CELL_BASIC_HEADER_HEIGHT;
}

static int16_t get_cell_height_beacons(MenuLayer *menu_layer, MenuIndex *cell_index, void *data) {
    return 26;
}

static void draw_beacon_header(GContext* ctx, const Layer *cell_layer, uint16_t section_index, void *data) {
    switch (section_index) {
        case 0:
            menu_cell_basic_header_draw(ctx, cell_layer, "Beacons in range");
            break;
        case 1:
            menu_cell_basic_header_draw(ctx, cell_layer, "Beacons out of range");
            break;
    }
}

static void draw_beacon_row(GContext *ctx, const Layer *cell_layer, MenuIndex *cell_index, void *callback_context) {
    switch (cell_index->section) {
        case 0:
            if(beacons_in_range!=NULL)
                menu_cell_basic_draw(ctx, cell_layer, beacons_in_range[cell_index->row].name, NULL, NULL);
            break;
        case 1:
            if(beacons_out_of_range!=NULL)
                menu_cell_basic_draw(ctx, cell_layer, beacons_out_of_range[cell_index->row].name, NULL, NULL);
            break;
    }
}

static void beacon_select_click(struct MenuLayer *menu_layer, MenuIndex *cell_index, void *callback_context) {
    char *new;
    if(cell_index->section==0 && beacons_in_range!=NULL) {
        if(current_beacon_name!=NULL)
            free(current_beacon_name);
        new = (char*)calloc(strlen(beacons_in_range[cell_index->row].name),sizeof(char));
        strcpy(new,beacons_in_range[cell_index->row].name);
        current_beacon_name = new;
    }
    else if(cell_index->section==1 && beacons_out_of_range!=NULL) {
        if(current_beacon_name!=NULL)
            free(current_beacon_name);
        new = (char*)calloc(strlen(beacons_out_of_range[cell_index->row].name),sizeof(char));
        strcpy(new,beacons_out_of_range[cell_index->row].name);
        current_beacon_name = new;
    }
    
    send_query_request(REQUEST_BEACON_DETAILS,current_beacon_name);
    
    waiting_for_info = WAITING_FOR_BEACON_DETAILS;
    window_stack_push(waiting_window, false);
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
    Layer *window_layer = window_get_root_layer(window);
    int textbar_height = 18;
    APP_LOG(APP_LOG_LEVEL_DEBUG, "-------- 1");
    
    GRect textbar_bounds = layer_get_bounds(window_layer);
    textbar_bounds.size.h = textbar_height;
    APP_LOG(APP_LOG_LEVEL_DEBUG, "-------- 2");
    beacons_textbar_layer = text_layer_create(textbar_bounds);
    APP_LOG(APP_LOG_LEVEL_DEBUG, "-------- 2.5");
    static char text_buffer[30];
    APP_LOG(APP_LOG_LEVEL_DEBUG, "-------- 3");
    snprintf(text_buffer,30,"%s (%u)",current_user_name,current_user_points);
    text_layer_set_text(beacons_textbar_layer,text_buffer);
    APP_LOG(APP_LOG_LEVEL_DEBUG, "-------- 4");
    text_layer_set_font(beacons_textbar_layer,fonts_get_system_font(FONT_KEY_GOTHIC_14));
    text_layer_set_text_alignment(beacons_textbar_layer, GTextAlignmentCenter);
    
    beacons_textbar_inverter_layer = inverter_layer_create(textbar_bounds);
    
    GRect menu_bounds = layer_get_bounds(window_layer);
    menu_bounds.size.h -= textbar_height;
    menu_bounds.origin.y += textbar_height;
    beacons_menu_layer = menu_layer_create(menu_bounds);
    menu_layer_set_callbacks(beacons_menu_layer, NULL, beacons_menu_callbacks);
    menu_layer_set_click_config_onto_window(beacons_menu_layer, window);
    
    layer_add_child(window_layer, text_layer_get_layer(beacons_textbar_layer));
    layer_add_child(window_layer, inverter_layer_get_layer(beacons_textbar_inverter_layer));
    layer_add_child(window_layer, menu_layer_get_layer(beacons_menu_layer));
}

static void beacons_window_unload(Window *window) {
    text_layer_destroy(beacons_textbar_layer);
    inverter_layer_destroy(beacons_textbar_inverter_layer);
    menu_layer_destroy(beacons_menu_layer);
}
/////////////////////////////////////

///////////////////////////////////// BEACON DETAILS WINDOW
static void dinstance_layer_update(Layer *layer, GContext *ctx) {
    int height = 34;
    int width = 136;
    graphics_context_set_stroke_color(ctx, GColorBlack);
    graphics_draw_round_rect(ctx,GRect(4,3,width,height),8);
    graphics_context_set_fill_color(ctx, GColorBlack);
    if(current_beacon_distance<96)
        graphics_fill_rect(ctx,GRect(4,3,width*current_beacon_distance/100.0,height),8,GCornersLeft);
    else if(current_beacon_distance>=96)
        graphics_fill_rect(ctx,GRect(4,3,width*current_beacon_distance/100.0,height),8,GCornersAll);
}

static void games_list_select_handler(ClickRecognizerRef recognizer, void *context) {
    send_simple_request(REQUEST_GAMES_ACTIVE);
    waiting_for_info = WAITING_FOR_GAMES;
    window_stack_push(waiting_window, false);
}

static void beacon_details_click_config_provider(void *context) {
    window_single_click_subscribe(BUTTON_ID_SELECT,(ClickHandler)games_list_select_handler);
}

static void beacon_details_window_load(Window *window) {
    Layer *window_layer = window_get_root_layer(window);
    int textbar_height = 18;
    int uppertext_layer_height = 30;
    int distance_layer_height = 40;
    
    GRect textbar_bounds = layer_get_bounds(window_layer);
    textbar_bounds.size.h = textbar_height;
    beacon_details_textbar_layer = text_layer_create(textbar_bounds);
    static char text_buffer1[30];
    snprintf(text_buffer1,30,"%s",current_beacon_name);
    text_layer_set_text(beacon_details_textbar_layer,text_buffer1);
    text_layer_set_font(beacon_details_textbar_layer,fonts_get_system_font(FONT_KEY_GOTHIC_14));
    text_layer_set_text_alignment(beacon_details_textbar_layer, GTextAlignmentCenter);
    
    beacon_details_textbar_inverter_layer = inverter_layer_create(textbar_bounds);
    
    GRect uppertext_bounds = layer_get_bounds(window_layer);
    uppertext_bounds.size.h = uppertext_layer_height;
    uppertext_bounds.origin.y = textbar_height;
    beacon_details_uppertext_layer = text_layer_create(uppertext_bounds);
    text_layer_set_text(beacon_details_uppertext_layer,"Distance:");
    text_layer_set_font(beacon_details_uppertext_layer,fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD));
    text_layer_set_text_alignment(beacon_details_uppertext_layer, GTextAlignmentCenter);
    
    GRect distance_layer_bounds = layer_get_bounds(window_layer);
    distance_layer_bounds.size.h = distance_layer_height;
    distance_layer_bounds.origin.y = textbar_height + uppertext_layer_height;
    beacon_details_distance_layer = layer_create(distance_layer_bounds);
    layer_set_update_proc(beacon_details_distance_layer, dinstance_layer_update);
    
    GRect lowertext_bounds = layer_get_bounds(window_layer);
    lowertext_bounds.size.h -= textbar_height + uppertext_layer_height + distance_layer_height;
    lowertext_bounds.origin.y = textbar_height + uppertext_layer_height + distance_layer_height;
    beacon_details_lowertext_layer = text_layer_create(lowertext_bounds);
    static char text_buffer2[70];
    snprintf(text_buffer2,70,"Active games: %u\nCompleted games: %u\n\nclick select for more...",current_beacon_active_games,current_beacon_completed_games);
    text_layer_set_text(beacon_details_lowertext_layer,text_buffer2);
    text_layer_set_font(beacon_details_lowertext_layer,fonts_get_system_font(FONT_KEY_GOTHIC_18));
    text_layer_set_text_alignment(beacon_details_lowertext_layer, GTextAlignmentLeft);
    
    layer_add_child(window_layer, text_layer_get_layer(beacon_details_textbar_layer));
    layer_add_child(window_layer, inverter_layer_get_layer(beacon_details_textbar_inverter_layer));
    layer_add_child(window_layer, text_layer_get_layer(beacon_details_uppertext_layer));
    layer_add_child(window_layer, beacon_details_distance_layer);
    layer_add_child(window_layer, text_layer_get_layer(beacon_details_lowertext_layer));
}

static void beacon_details_window_unload(Window *window) {
    text_layer_destroy(beacon_details_textbar_layer);
    inverter_layer_destroy(beacon_details_textbar_inverter_layer);
    text_layer_destroy(beacon_details_uppertext_layer);
    layer_destroy(beacon_details_distance_layer);
    text_layer_destroy(beacon_details_lowertext_layer);
}
/////////////////////////////////////

///////////////////////////////////// GAMES WINDOW
static uint16_t get_num_sections_games(MenuLayer *menu_layer, void *data) {
    return 2;
}

static int16_t get_header_height_games(MenuLayer *menu_layer, uint16_t section_index, void *data) {
    return MENU_CELL_BASIC_HEADER_HEIGHT;
}

static int16_t get_cell_height_games(MenuLayer *menu_layer, MenuIndex *cell_index, void *data) {
    return 26;
}

static void draw_game_header(GContext* ctx, const Layer *cell_layer, uint16_t section_index, void *data) {
    switch (section_index) {
        case 0:
            menu_cell_basic_header_draw(ctx, cell_layer, "Active games");
            break;
        case 1:
            menu_cell_basic_header_draw(ctx, cell_layer, "Completed games");
            break;
    }
}

static void draw_game_row(GContext *ctx, const Layer *cell_layer, MenuIndex *cell_index, void *callback_context) {
    switch (cell_index->section) {
        case 0:
            if(games_active!=NULL)
                menu_cell_basic_draw(ctx, cell_layer, games_active[cell_index->row].name, NULL, NULL);
            break;
        case 1:
            if(games_completed!=NULL)
                menu_cell_basic_draw(ctx, cell_layer, games_completed[cell_index->row].name, NULL, NULL);
            break;
    }
}

static void game_select_click(struct MenuLayer *menu_layer, MenuIndex *cell_index, void *callback_context) {
    if(cell_index->section==0 && games_active!=NULL)
        current_game_name = games_active[cell_index->row].name;
    else if(cell_index->section==1 && games_completed!=NULL)
        current_game_name = games_completed[cell_index->row].name;
    else
        current_game_name = NULL;
    
    send_query_request(REQUEST_GAME_DETAILS,current_game_name);
    
    waiting_for_info = WAITING_FOR_GAME_DETAILS;
    window_stack_push(waiting_window, false);
}

MenuLayerCallbacks games_menu_callbacks = {
    .get_num_sections = get_num_sections_games,
    .get_num_rows = get_num_games,
    .get_header_height = get_header_height_games,
    .get_cell_height = get_cell_height_games,
    .draw_header = draw_game_header,
    .draw_row = draw_game_row,
    .select_click = game_select_click
};

static void games_window_load(Window *window) {
    Layer *window_layer = window_get_root_layer(window);
    int textbar_height = 18;
    
    GRect textbar_bounds = layer_get_bounds(window_layer);
    textbar_bounds.size.h = textbar_height;
    games_textbar_layer = text_layer_create(textbar_bounds);
    static char text_buffer[30];
    snprintf(text_buffer,30,"%s",current_beacon_name);
    text_layer_set_text(games_textbar_layer,text_buffer);
    text_layer_set_font(games_textbar_layer,fonts_get_system_font(FONT_KEY_GOTHIC_14));
    text_layer_set_text_alignment(games_textbar_layer, GTextAlignmentCenter);
    
    games_textbar_inverter_layer = inverter_layer_create(textbar_bounds);
    
    GRect menu_bounds = layer_get_bounds(window_layer);
    menu_bounds.size.h -= textbar_height;
    menu_bounds.origin.y += textbar_height;
    games_menu_layer = menu_layer_create(menu_bounds);
    menu_layer_set_callbacks(games_menu_layer, NULL, games_menu_callbacks);
    menu_layer_set_click_config_onto_window(games_menu_layer, window);
    
    layer_add_child(window_layer, text_layer_get_layer(games_textbar_layer));
    layer_add_child(window_layer, inverter_layer_get_layer(games_textbar_inverter_layer));
    layer_add_child(window_layer, menu_layer_get_layer(games_menu_layer));
}

static void games_window_unload(Window *window) {
    text_layer_destroy(games_textbar_layer);
    inverter_layer_destroy(games_textbar_inverter_layer);
    menu_layer_destroy(games_menu_layer);
}
/////////////////////////////////////

///////////////////////////////////// GAME DETAILS WINDOW
static void game_details_window_load(Window *window) {
    Layer *window_layer = window_get_root_layer(window);
    int textbar_height = 18;
    
    GRect textbar_bounds = layer_get_bounds(window_layer);
    textbar_bounds.size.h = textbar_height;
    game_details_textbar_layer = text_layer_create(textbar_bounds);
    static char text_buffer1[30];
    snprintf(text_buffer1,30,"%s",current_game_name);
    text_layer_set_text(game_details_textbar_layer,text_buffer1);
    text_layer_set_font(game_details_textbar_layer,fonts_get_system_font(FONT_KEY_GOTHIC_14));
    text_layer_set_text_alignment(game_details_textbar_layer, GTextAlignmentCenter);
    
    game_details_textbar_inverter_layer = inverter_layer_create(textbar_bounds);
    
    GRect text_bounds = layer_get_bounds(window_layer);
    text_bounds.size.h -= textbar_height;
    text_bounds.origin.y = textbar_height;
    game_details_text_layer = text_layer_create(text_bounds);
    text_layer_set_text(game_details_text_layer,current_game_description);
    text_layer_set_font(game_details_text_layer,fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD));
    text_layer_set_text_alignment(game_details_text_layer, GTextAlignmentCenter);
    
    layer_add_child(window_layer, text_layer_get_layer(game_details_textbar_layer));
    layer_add_child(window_layer, inverter_layer_get_layer(game_details_textbar_inverter_layer));
    layer_add_child(window_layer, text_layer_get_layer(game_details_text_layer));
}

static void game_details_window_unload(Window *window) {
    text_layer_destroy(game_details_textbar_layer);
    inverter_layer_destroy(game_details_textbar_inverter_layer);
    text_layer_destroy(game_details_text_layer);
}
/////////////////////////////////////

///////////////////////////////////// WAITING WINDOW
static void waiting_window_load(Window *window) {
    Layer *window_layer = window_get_root_layer(window);
    int textbar_height = 18;
    
    GRect textbar_bounds = layer_get_bounds(window_layer);
    textbar_bounds.size.h = textbar_height;
    waiting_textbar_layer = text_layer_create(textbar_bounds);
    static char text_buffer1[30];
    if(waiting_for_info==WAITING_FOR_BEACONS)
        snprintf(text_buffer1,30,"%s (%u)",current_user_name,current_user_points);
    else if(waiting_for_info==WAITING_FOR_BEACON_DETAILS)
        snprintf(text_buffer1,30,"%s",current_beacon_name);
    else if(waiting_for_info==WAITING_FOR_GAMES)
        snprintf(text_buffer1,30,"%s",current_beacon_name);
    else if(waiting_for_info==WAITING_FOR_GAME_DETAILS)
        snprintf(text_buffer1,30,"%s",current_game_name);
    else if(waiting_for_info==WAITING_FOR_USER_DETAILS)
        snprintf(text_buffer1,30,"%s",current_user_name);
    else if(waiting_for_info==WAITING_FOR_ACHIEVEMENT_DETAILS)
        snprintf(text_buffer1,30,"%s",current_achievement_name);
    text_layer_set_text(waiting_textbar_layer,text_buffer1);
    text_layer_set_font(waiting_textbar_layer,fonts_get_system_font(FONT_KEY_GOTHIC_14));
    text_layer_set_text_alignment(waiting_textbar_layer, GTextAlignmentCenter);
    
    waiting_textbar_inverter_layer = inverter_layer_create(textbar_bounds);
    
    GRect text_bounds = layer_get_bounds(window_layer);
    text_bounds.size.h -= textbar_height;
    text_bounds.origin.y += textbar_height;
    waiting_text_layer = text_layer_create(text_bounds);
    static char text_buffer2[60];
    if(waiting_for_info==WAITING_FOR_BEACONS)
        snprintf(text_buffer2,60,"\nPlease wait, beacons list is being transmitted...");
    else if(waiting_for_info==WAITING_FOR_BEACON_DETAILS)
        snprintf(text_buffer2,60,"\nPlease wait, beacon details are being transmitted...");
    else if(waiting_for_info==WAITING_FOR_GAMES)
        snprintf(text_buffer2,60,"\nPlease wait, games list is being transmitted...");
    else if(waiting_for_info==WAITING_FOR_GAME_DETAILS)
        snprintf(text_buffer2,60,"\nPlease wait, game details are being transmitted...");
    else if(waiting_for_info==WAITING_FOR_USER_DETAILS)
        snprintf(text_buffer2,60,"\nPlease wait, user details are being transmitted...");
    else if(waiting_for_info==WAITING_FOR_ACHIEVEMENT_DETAILS)
        snprintf(text_buffer2,60,"\nPlease wait, achievement details are being transmitted...");
    text_layer_set_text(waiting_text_layer,text_buffer2);
    text_layer_set_font(waiting_text_layer,fonts_get_system_font(FONT_KEY_GOTHIC_24));
    text_layer_set_text_alignment(waiting_text_layer, GTextAlignmentCenter);
    
    layer_add_child(window_layer, text_layer_get_layer(waiting_textbar_layer));
    layer_add_child(window_layer, inverter_layer_get_layer(waiting_textbar_inverter_layer));
    layer_add_child(window_layer, text_layer_get_layer(waiting_text_layer));
}

static void waiting_window_unload(Window *window) {
    text_layer_destroy(waiting_textbar_layer);
    inverter_layer_destroy(waiting_textbar_inverter_layer);
    text_layer_destroy(waiting_text_layer);
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
static WindowHandlers beacon_details_window_handlers = {
    .load = beacon_details_window_load,
    .unload = beacon_details_window_unload
};
static WindowHandlers games_window_handlers = {
    .load = games_window_load,
    .unload = games_window_unload
};
static WindowHandlers game_details_window_handlers = {
    .load = game_details_window_load,
    .unload = game_details_window_unload
};
static WindowHandlers waiting_window_handlers = {
    .load = waiting_window_load,
    .unload = waiting_window_unload
};
/////////////////////////////////////

/////////////////////////////////////
static void init() {
    timer = NULL;
    //beacons_in_range = NULL;
    //beacons_out_of_range = NULL;
    //beacons_in_range_amount = 0;
    //beacons_out_of_range_amount = 0;
    current_user_name = NULL;
    current_beacon_name = NULL;
    current_game_name = NULL;
    current_game_description = NULL;
    current_achievement_name = NULL;
    current_beacon_distance = 0;
    current_beacon_active_games = 0;
    current_beacon_completed_games = 0;
    waiting_for_info = 0;
    current_user_points = 0;
    
    login_window = window_create();
    window_set_fullscreen(login_window, true);
    window_set_window_handlers(login_window, login_window_handlers);
    
    user_window = window_create();
    window_set_fullscreen(user_window, true);
    window_set_window_handlers(user_window, user_window_handlers);
    
    beacons_window = window_create();
    window_set_fullscreen(beacons_window, true);
    window_set_window_handlers(beacons_window, beacons_window_handlers);
    
    beacon_details_window = window_create();
    window_set_fullscreen(beacon_details_window, true);
    window_set_window_handlers(beacon_details_window, beacon_details_window_handlers);
    window_set_click_config_provider(beacon_details_window, beacon_details_click_config_provider);
    
    games_window = window_create();
    window_set_fullscreen(games_window, true);
    window_set_window_handlers(games_window, games_window_handlers);
    
    game_details_window = window_create();
    window_set_fullscreen(game_details_window, true);
    window_set_window_handlers(game_details_window, game_details_window_handlers);
    
    waiting_window = window_create();
    window_set_fullscreen(waiting_window, true);
    window_set_window_handlers(waiting_window, waiting_window_handlers);
    
    app_message_register_inbox_received(in_received_handler);
    app_message_register_inbox_dropped(in_dropped_handler);
    app_message_register_outbox_sent(out_sent_handler);
    app_message_register_outbox_failed(out_failed_handler);
    
    const int inbound_size = APP_MESSAGE_INBOX_SIZE_MINIMUM;
    const int outbound_size = APP_MESSAGE_OUTBOX_SIZE_MINIMUM;
    app_message_open(inbound_size,outbound_size);
    
    window_stack_push(login_window, true);
}

static void deinit() {
    window_destroy(login_window);
    window_destroy(user_window);
    window_destroy(beacons_window);
    window_destroy(beacon_details_window);
    window_destroy(games_window);
    window_destroy(game_details_window);
    window_destroy(waiting_window);
    
    free(current_user_name);
    free(current_game_description);
    free(current_achievement_name);
    if(beacons_in_range!=NULL) {
        int i;
        for(i=0; i<beacons_in_range_amount; ++i)
            free(beacons_in_range[i].name);
        free(beacons_in_range);
    }
    if(beacons_out_of_range!=NULL) {
        int i;
        for(i=0; i<beacons_out_of_range_amount; ++i)
            free(beacons_out_of_range[i].name);
        free(beacons_out_of_range);
    }
    if(games_active!=NULL) {
        int i;
        for(i=0; i<games_active_amount; ++i)
            free(games_active[i].name);
        free(games_active);
    }
    if(games_completed!=NULL) {
        int i;
        for(i=0; i<games_completed_amount; ++i)
            free(games_completed[i].name);
        free(games_completed);
    }
   if(achievements!=NULL) {
        int i;
        for(i=0; i<achievements_amount; ++i)
            free(achievements[i].name);
        free(achievements);
    }    
}
/////////////////////////////////////

int main(void) {
    init();
    app_event_loop();
    deinit();
}
