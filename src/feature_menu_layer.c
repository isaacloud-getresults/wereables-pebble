///    @author: Pawel Palka
///    @email: ppalka@sosoftware.pl    // company
///    @email: fractalord@gmail.com    // private

#include "pebble.h"

/////////////////////////////////////
static Window *login_window;
static Window *beacons_window;
static Window *beacon_details_window;
static Window *games_window;
static Window *game_details_window;
static Window *waiting_window;
static MenuLayer *beacons_menu_layer;
static MenuLayer *games_menu_layer;
static TextLayer *login_uppertext_layer;
static TextLayer *login_lowertext_layer;
static TextLayer *beacons_textbar_layer;
static TextLayer *beacon_details_textbar_layer;
static TextLayer *beacon_details_uppertext_layer;
static TextLayer *beacon_details_lowertext_layer;
static TextLayer *waiting_textbar_layer;
static TextLayer *waiting_text_layer;
static InverterLayer *beacons_textbar_inverter_layer;
static InverterLayer *beacon_details_textbar_inverter_layer;
static InverterLayer *waiting_textbar_inverter_layer;
static Layer *beacon_details_distance_layer;

static AppTimer *timer;
static char *current_user_name;
static char *current_beacon_name;
static char *current_game_name;
static uint8_t current_beacon_distance;
static uint8_t current_beacon_active_games;
static uint8_t current_beacon_completed_games;
static uint8_t waiting_for_info;
/////////////////////////////////////

///////////////////////////////////// USERS INITIALISING
typedef struct {
    char *name;
    //uint16_t id;
} User;
/////////////////////////////////////

///////////////////////////////////// BEACONS INITIALISING
typedef struct {
    char *name;
    //uint16_t uuid;
} Beacon;

Beacon *beacons_in_range;
Beacon *beacons_out_of_range;
static int beacons_in_range_amount = 0;
static int beacons_out_of_range_amount = 0;

static uint16_t get_num_beacons_in_range(void) {
    return beacons_in_range_amount;
}

static uint16_t get_num_beacons_out_of_range(void) {
    return beacons_out_of_range_amount;
}

static uint16_t get_num_beacons(struct MenuLayer* menu_layer, uint16_t section_index, void *callback_context) {
    uint16_t num = 0;
    if(section_index==0)
        num = get_num_beacons_in_range();
    else if(section_index==1)
        num = get_num_beacons_out_of_range();
    return num;
}
/////////////////////////////////////

///////////////////////////////////// COMMUNICATION
enum { // actualise, not every position is needed
    REQUEST = 1,
    REQUEST_QUERY = 2,
    REQUEST_USERS = 11,
    REQUEST_BEACONS_IN_RANGE = 12,
    REQUEST_BEACONS_OUT_OF_RANGE = 13,
    REQUEST_GAMES_ACTIVE = 14,
    REQUEST_GAMES_COMPLETED = 15,
    REQUEST_LOGIN = 16,
    REQUEST_BEACON_DETAILS = 17,
    REQUEST_PROGRESS = 18,
    RESPONSE_TYPE = 200,
    RESPONSE_LENGTH = 201,
    RESPONSE_USERS = 21,
    RESPONSE_BEACONS_IN_RANGE = 22,
    RESPONSE_BEACONS_OUT_OF_RANGE = 23,
    RESPONSE_GAMES_ACTIVE = 24,
    RESPONSE_GAMES_COMPLETED = 25,
    RESPONSE_LOGIN = 26,
    RESPONSE_BEACON_DETAILS = 27,
    RESPONSE_PROGRESS = 28
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
            if(beacons_in_range!=NULL) {
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
            if(beacons_out_of_range!=NULL) {
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
            window_stack_push(beacons_window, true);
            window_stack_remove(waiting_window, false);
            window_stack_remove(login_window, false);
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
        else if(receiving_type->value->data[0]==RESPONSE_LOGIN && current_user_name==NULL) {
            Tuple *tuple = dict_find(iter,0);
            char *new = (char*)calloc(strlen(tuple->value->cstring),sizeof(char));
            strcpy(new,tuple->value->cstring);
            current_user_name = new;
            APP_LOG(APP_LOG_LEVEL_DEBUG, "User received: %s",current_user_name);
            static char text_buffer[40];
            snprintf(text_buffer,40,"Welcome %s\ndownloading data...",current_user_name);
            text_layer_set_text(login_lowertext_layer,text_buffer);
            send_simple_request(REQUEST_BEACONS_IN_RANGE);
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
static void login_request_sending(void *data) {
    if(current_user_name==NULL) {
        send_simple_request(REQUEST_LOGIN);
        timer = app_timer_register(1000,login_request_sending,NULL);
    }
}


static void login_window_load(Window *window) {
    Layer *window_layer = window_get_root_layer(window);
    int uppertext_height = 80;
    
    timer = app_timer_register(1000,login_request_sending,NULL);
    send_simple_request(REQUEST_LOGIN);
    
    GRect uppertext_bounds = layer_get_bounds(window_layer);
    uppertext_bounds.size.h = uppertext_height;
    login_uppertext_layer = text_layer_create(uppertext_bounds);
    text_layer_set_text(login_uppertext_layer,"SoI Beacons");
    text_layer_set_font(login_uppertext_layer,fonts_get_system_font(FONT_KEY_BITHAM_30_BLACK));
    text_layer_set_text_alignment(login_uppertext_layer, GTextAlignmentCenter);
    
    GRect lowertext_bounds = layer_get_bounds(window_layer);
    lowertext_bounds.size.h -= uppertext_height;
    lowertext_bounds.origin.y += uppertext_height;
    login_lowertext_layer = text_layer_create(lowertext_bounds);
    text_layer_set_text(login_lowertext_layer,"connecting with your smartphone...");
    text_layer_set_font(login_lowertext_layer,fonts_get_system_font(FONT_KEY_GOTHIC_24));
    text_layer_set_text_alignment(login_lowertext_layer, GTextAlignmentCenter);
    
    layer_add_child(window_layer, text_layer_get_layer(login_uppertext_layer));
    layer_add_child(window_layer, text_layer_get_layer(login_lowertext_layer));
}

static void login_window_unload(Window *window) {
    text_layer_destroy(login_uppertext_layer);
    text_layer_destroy(login_lowertext_layer);
}
/////////////////////////////////////

///////////////////////////////////// BEACONS WINDOW
static uint16_t get_num_sections_beacons(MenuLayer *menu_layer, void *data) {
    return 2;
}

static int16_t beacons_get_header_height(MenuLayer *menu_layer, uint16_t section_index, void *data) {
    return MENU_CELL_BASIC_HEADER_HEIGHT;
}

static int16_t beacons_get_cell_height(MenuLayer *menu_layer, MenuIndex *cell_index, void *data) {
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
    if(cell_index->section==0 && beacons_in_range!=NULL)
        current_beacon_name = beacons_in_range[cell_index->row].name;
    else if(cell_index->section==1 && beacons_out_of_range!=NULL)
        current_beacon_name = beacons_out_of_range[cell_index->row].name;
    else 
        current_beacon_name = NULL;
   
    send_query_request(REQUEST_BEACON_DETAILS,current_beacon_name);
    
    waiting_for_info = 2;
    window_stack_push(waiting_window, false);
}

MenuLayerCallbacks beacons_menu_callbacks = {
    .get_num_sections = get_num_sections_beacons,
    .get_num_rows = get_num_beacons,
    .get_header_height = beacons_get_header_height,
    .get_cell_height = beacons_get_cell_height, 
    .draw_header = draw_beacon_header,
    .draw_row = draw_beacon_row,
    .select_click = beacon_select_click
};

static void beacons_window_load(Window *window) {
    Layer *window_layer = window_get_root_layer(window);
    int textbar_height = 18;
    
    GRect textbar_bounds = layer_get_bounds(window_layer);
    textbar_bounds.size.h = textbar_height;
    beacons_textbar_layer = text_layer_create(textbar_bounds);
    static char text_buffer[30];
    snprintf(text_buffer,30,"%s",current_user_name);
    text_layer_set_text(beacons_textbar_layer,text_buffer);
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

///////////////////////////////////// WAITING WINDOW
static void waiting_window_load(Window *window) {
    Layer *window_layer = window_get_root_layer(window);
    int textbar_height = 18;
    
    GRect textbar_bounds = layer_get_bounds(window_layer);
    textbar_bounds.size.h = textbar_height;
    waiting_textbar_layer = text_layer_create(textbar_bounds);
    static char text_buffer[30];
    if(waiting_for_info==2)
        snprintf(text_buffer,30,"%s",current_beacon_name);
    text_layer_set_text(waiting_textbar_layer,text_buffer);
    text_layer_set_font(waiting_textbar_layer,fonts_get_system_font(FONT_KEY_GOTHIC_14));
    text_layer_set_text_alignment(waiting_textbar_layer, GTextAlignmentCenter);
    
    waiting_textbar_inverter_layer = inverter_layer_create(textbar_bounds);
    
    GRect text_bounds = layer_get_bounds(window_layer);
    text_bounds.size.h -= textbar_height;
    text_bounds.origin.y += textbar_height;
    waiting_text_layer = text_layer_create(text_bounds);
    static char text_buffer2[60];
    if(waiting_for_info==2)
        snprintf(text_buffer2,60,"\nPlease wait, beacon details are being transmitted...");
    text_layer_set_text(waiting_text_layer,text_buffer2);
    text_layer_set_font(waiting_text_layer,fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD));
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
static WindowHandlers beacons_window_handlers = {
    .load = beacons_window_load,
    .unload = beacons_window_unload
};
static WindowHandlers beacon_details_window_handlers = {
    .load = beacon_details_window_load,
    .unload = beacon_details_window_unload
};
static WindowHandlers games_window_handlers = {
    .load = NULL,
    .unload = NULL
};
static WindowHandlers game_details_window_handlers = {
    .load = NULL,
    .unload = NULL
};
static WindowHandlers waiting_window_handlers = {
    .load = waiting_window_load,
    .unload = waiting_window_unload
};
/////////////////////////////////////

/////////////////////////////////////
static void init() {
    timer = NULL;
    beacons_in_range = NULL;
    beacons_out_of_range = NULL;
    beacons_in_range_amount = 0;
    beacons_out_of_range_amount = 0;
    current_user_name = NULL;
    current_beacon_name = "NO BEACON";
    current_game_name = "NO GAME";
    current_beacon_distance = 0;
    current_beacon_active_games = 0;
    current_beacon_completed_games = 0;
    waiting_for_info = 0;
    
    login_window = window_create();
    window_set_fullscreen(login_window, true);
    window_set_window_handlers(login_window, login_window_handlers);
    
    beacons_window = window_create();
    window_set_fullscreen(beacons_window, true);
    window_set_window_handlers(beacons_window, beacons_window_handlers);
    
    beacon_details_window = window_create();
    window_set_fullscreen(beacon_details_window, true);
    window_set_window_handlers(beacon_details_window, beacon_details_window_handlers);
    
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
    window_destroy(beacons_window);
    window_destroy(beacon_details_window);
    window_destroy(games_window);
    window_destroy(game_details_window);
    window_destroy(waiting_window);
    
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
    free(current_user_name);
}
/////////////////////////////////////

int main(void) {
    init();
    app_event_loop();
    deinit();
}
